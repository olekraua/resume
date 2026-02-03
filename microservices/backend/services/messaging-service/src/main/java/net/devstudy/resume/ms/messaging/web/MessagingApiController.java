package net.devstudy.resume.ms.messaging.web;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.auth.api.security.CurrentProfileProvider;
import net.devstudy.resume.messaging.api.model.Conversation;
import net.devstudy.resume.messaging.api.model.ConversationParticipant;
import net.devstudy.resume.messaging.api.model.ConversationType;
import net.devstudy.resume.messaging.api.model.Message;
import net.devstudy.resume.messaging.api.model.MessageAttachment;
import net.devstudy.resume.messaging.internal.component.MessageAttachmentStorage;
import net.devstudy.resume.messaging.internal.component.MessageAttachmentStorage.StoredAttachment;
import net.devstudy.resume.messaging.internal.config.MessageAttachmentProperties;
import net.devstudy.resume.messaging.internal.repository.storage.ConversationParticipantRepository;
import net.devstudy.resume.messaging.internal.repository.storage.ConversationRepository;
import net.devstudy.resume.messaging.internal.repository.storage.MessageAttachmentRepository;
import net.devstudy.resume.messaging.internal.repository.storage.MessageRepository;
import net.devstudy.resume.web.api.ApiErrorUtils;

@RestController
@RequestMapping("/api/messages")
public class MessagingApiController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final MessageAttachmentStorage attachmentStorage;
    private final MessageAttachmentProperties attachmentProperties;
    private final CurrentProfileProvider currentProfileProvider;
    private final SimpMessagingTemplate messagingTemplate;

    public MessagingApiController(ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            MessageRepository messageRepository,
            MessageAttachmentRepository attachmentRepository,
            MessageAttachmentStorage attachmentStorage,
            MessageAttachmentProperties attachmentProperties,
            CurrentProfileProvider currentProfileProvider,
            SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.attachmentStorage = attachmentStorage;
        this.attachmentProperties = attachmentProperties;
        this.currentProfileProvider = currentProfileProvider;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/conversations")
    @Transactional
    public ResponseEntity<?> createConversation(@RequestBody(required = false) CreateConversationRequest request,
            HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        Long otherId = request == null ? null : request.otherProfileId();
        if (otherId == null) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Other profile id is required", httpRequest);
        }
        if (otherId.equals(currentId)) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Cannot start conversation with yourself", httpRequest);
        }
        Conversation conversation = findOrCreateDirectConversation(currentId, otherId);
        Long resolvedOther = resolveOtherParticipant(conversation.getId(), currentId);
        return ResponseEntity.ok(new ConversationResponse(conversation.getId(), resolvedOther));
    }

    @GetMapping("/conversations")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listConversations(HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        List<ConversationParticipant> participants = participantRepository.findByProfileId(currentId);
        if (participants.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        Set<Long> conversationIds = participants.stream()
                .map(p -> p.getConversation().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Conversation> conversations = conversationRepository.findAllById(conversationIds).stream()
                .collect(Collectors.toMap(Conversation::getId, c -> c));
        List<ConversationParticipant> allParticipants = participantRepository.findByConversationIdIn(conversationIds);
        Map<Long, List<ConversationParticipant>> byConversation = allParticipants.stream()
                .collect(Collectors.groupingBy(p -> p.getConversation().getId()));
        List<ConversationSummary> summaries = new ArrayList<>();
        for (ConversationParticipant participant : participants) {
            Conversation conversation = participant.getConversation();
            if (conversation == null) {
                continue;
            }
            Conversation stored = conversations.get(conversation.getId());
            if (stored == null) {
                continue;
            }
            Long otherId = resolveOtherParticipant(byConversation.get(stored.getId()), currentId);
            MessageItem lastMessage = null;
            if (stored.getLastMessageId() != null) {
                Message message = messageRepository.findById(stored.getLastMessageId()).orElse(null);
                if (message != null) {
                    List<MessageAttachment> attachments = attachmentRepository
                            .findByMessageIdIn(List.of(message.getId()));
                    lastMessage = toMessageItem(message, attachments);
                }
            }
            long unread = countUnread(stored.getId(), currentId, participant.getLastReadMessageId());
            summaries.add(new ConversationSummary(stored.getId(), otherId, lastMessage, unread));
        }
        summaries.sort(Comparator.comparing(ConversationSummary::lastMessageAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/conversations/{id}/messages")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listMessages(@PathVariable("id") Long conversationId,
            @RequestParam(name = "beforeId", required = false) Long beforeId,
            @RequestParam(name = "size", required = false) Integer size,
            HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        if (!isParticipant(conversationId, currentId)) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Conversation not found", httpRequest);
        }
        int pageSize = resolvePageSize(size);
        Page<Message> page;
        if (beforeId != null) {
            page = messageRepository.findByConversationIdAndIdLessThanOrderByCreatedDesc(
                    conversationId,
                    beforeId,
                    PageRequest.of(0, pageSize)
            );
        } else {
            page = messageRepository.findByConversationIdOrderByCreatedDesc(
                    conversationId,
                    PageRequest.of(0, pageSize)
            );
        }
        List<Message> messages = page.getContent();
        if (messages.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Long> ids = messages.stream().map(Message::getId).toList();
        Map<Long, List<MessageAttachment>> attachmentsByMessage = attachmentRepository.findByMessageIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getMessage().getId()));
        List<MessageItem> items = messages.stream()
                .map(message -> toMessageItem(message, attachmentsByMessage.get(message.getId())))
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/conversations/{id}/messages")
    @Transactional
    public ResponseEntity<?> sendMessage(@PathVariable("id") Long conversationId,
            @RequestBody(required = false) SendMessageRequest request,
            HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null || !isParticipant(conversationId, currentId)) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Conversation not found", httpRequest);
        }
        String body = request == null ? null : normalizeBody(request.body());
        List<Long> attachmentIds = request == null ? List.of() : normalizeAttachmentIds(request.attachmentIds());
        if ((body == null || body.isBlank()) && attachmentIds.isEmpty()) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Message body is empty", httpRequest);
        }
        List<MessageAttachment> attachments = new ArrayList<>();
        if (!attachmentIds.isEmpty()) {
            List<MessageAttachment> stored = attachmentRepository.findAllById(attachmentIds);
            if (stored.size() != attachmentIds.size()) {
                return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment not found", httpRequest);
            }
            for (MessageAttachment attachment : stored) {
                if (!conversationId.equals(attachment.getConversationId())) {
                    return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment does not belong to conversation",
                            httpRequest);
                }
                if (!currentId.equals(attachment.getUploaderId())) {
                    return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment uploader mismatch", httpRequest);
                }
                if (attachment.getMessage() != null) {
                    return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment already linked", httpRequest);
                }
            }
            attachments.addAll(stored);
        }
        Message message = new Message(conversation, currentId, body, Instant.now());
        messageRepository.save(message);
        if (!attachments.isEmpty()) {
            for (MessageAttachment attachment : attachments) {
                attachment.setMessage(message);
            }
            attachmentRepository.saveAll(attachments);
        }
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageAt(message.getCreated());
        conversationRepository.save(conversation);
        updateLastRead(conversationId, currentId, message.getId());
        MessageItem messageItem = toMessageItem(message, attachments);
        publishAfterCommit(() -> messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId + "/messages",
                messageItem
        ));
        return ResponseEntity.ok(messageItem);
    }

    @PostMapping("/conversations/{id}/read")
    @Transactional
    public ResponseEntity<?> markRead(@PathVariable("id") Long conversationId,
            @RequestBody(required = false) ReadRequest request,
            HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        if (!isParticipant(conversationId, currentId)) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Conversation not found", httpRequest);
        }
        Long lastReadMessageId = request == null ? null : request.lastReadMessageId();
        if (lastReadMessageId == null) {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation != null) {
                lastReadMessageId = conversation.getLastMessageId();
            }
        }
        if (lastReadMessageId == null) {
            return ResponseEntity.noContent().build();
        }
        updateLastRead(conversationId, currentId, lastReadMessageId);
        ReadEvent event = new ReadEvent(conversationId, currentId, lastReadMessageId);
        Long finalLastReadMessageId = lastReadMessageId;
        publishAfterCommit(() -> messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId + "/read",
                new ReadEvent(conversationId, currentId, finalLastReadMessageId)
        ));
        return ResponseEntity.ok(event);
    }

    @PostMapping("/conversations/{id}/attachments")
    @Transactional
    public ResponseEntity<?> uploadAttachment(@PathVariable("id") Long conversationId,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        if (!isParticipant(conversationId, currentId)) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Conversation not found", httpRequest);
        }
        if (file == null || file.isEmpty()) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment is empty", httpRequest);
        }
        long maxBytes = attachmentProperties.getMaxSize().toBytes();
        if (file.getSize() > maxBytes) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment exceeds max size", httpRequest);
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!isAllowedType(contentType)) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Attachment type not allowed", httpRequest);
        }
        StoredAttachment stored = attachmentStorage.store(file);
        MessageAttachment attachment = new MessageAttachment(
                conversationId,
                currentId,
                stored.storageKey(),
                stored.originalName(),
                stored.contentType(),
                stored.size(),
                Instant.now()
        );
        attachmentRepository.save(attachment);
        return ResponseEntity.ok(toAttachmentItem(attachment));
    }

    @GetMapping("/attachments/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> downloadAttachment(@PathVariable("id") Long attachmentId,
            HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        MessageAttachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
        if (attachment == null || attachment.getMessage() == null) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Attachment not found", httpRequest);
        }
        if (!isParticipant(attachment.getConversationId(), currentId)) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Attachment not found", httpRequest);
        }
        Path path = attachmentStorage.resolvePath(attachment.getStorageKey());
        if (path == null || !path.toFile().exists()) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Attachment not found", httpRequest);
        }
        Resource resource = new FileSystemResource(path);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                .build();
        MediaType mediaType = resolveMediaType(attachment.getContentType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .contentLength(attachment.getSize())
                .body(resource);
    }

    @GetMapping("/unread-count")
    @Transactional(readOnly = true)
    public ResponseEntity<?> unreadCount(HttpServletRequest httpRequest) {
        Long currentId = requireCurrentId();
        if (currentId == null) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Unauthorized", httpRequest);
        }
        List<ConversationParticipant> participants = participantRepository.findByProfileId(currentId);
        if (participants.isEmpty()) {
            return ResponseEntity.ok(new UnreadCountResponse(0));
        }
        long total = 0;
        for (ConversationParticipant participant : participants) {
            Conversation conversation = participant.getConversation();
            if (conversation == null) {
                continue;
            }
            total += countUnread(conversation.getId(), currentId, participant.getLastReadMessageId());
        }
        return ResponseEntity.ok(new UnreadCountResponse(total));
    }

    private Long requireCurrentId() {
        return currentProfileProvider.getCurrentId();
    }

    private Conversation findOrCreateDirectConversation(Long firstId, Long secondId) {
        String pairKey = toPairKey(firstId, secondId);
        Conversation existing = conversationRepository.findByPairKey(pairKey).orElse(null);
        if (existing != null) {
            ensureParticipants(existing, firstId, secondId);
            return existing;
        }
        Conversation conversation = new Conversation(ConversationType.DIRECT, pairKey, Instant.now());
        try {
            Conversation stored = conversationRepository.save(conversation);
            ensureParticipants(stored, firstId, secondId);
            return stored;
        } catch (DataIntegrityViolationException ex) {
            Conversation stored = conversationRepository.findByPairKey(pairKey).orElseThrow(() -> ex);
            ensureParticipants(stored, firstId, secondId);
            return stored;
        }
    }

    private void ensureParticipants(Conversation conversation, Long firstId, Long secondId) {
        if (conversation == null) {
            return;
        }
        addParticipantIfMissing(conversation, firstId);
        addParticipantIfMissing(conversation, secondId);
    }

    private void addParticipantIfMissing(Conversation conversation, Long profileId) {
        if (profileId == null) {
            return;
        }
        if (participantRepository.findByConversationIdAndProfileId(conversation.getId(), profileId).isPresent()) {
            return;
        }
        ConversationParticipant participant = new ConversationParticipant(conversation, profileId);
        participantRepository.save(participant);
    }

    private boolean isParticipant(Long conversationId, Long profileId) {
        if (conversationId == null || profileId == null) {
            return false;
        }
        return participantRepository.findByConversationIdAndProfileId(conversationId, profileId).isPresent();
    }

    private void updateLastRead(Long conversationId, Long profileId, Long lastReadMessageId) {
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndProfileId(conversationId, profileId)
                .orElse(null);
        if (participant == null) {
            return;
        }
        participant.setLastReadMessageId(lastReadMessageId);
        participant.setLastReadAt(Instant.now());
        participantRepository.save(participant);
    }

    private long countUnread(Long conversationId, Long profileId, Long lastReadMessageId) {
        if (conversationId == null || profileId == null) {
            return 0;
        }
        if (lastReadMessageId == null) {
            return messageRepository.countByConversationIdAndSenderIdNot(conversationId, profileId);
        }
        return messageRepository.countByConversationIdAndIdGreaterThanAndSenderIdNot(
                conversationId,
                lastReadMessageId,
                profileId
        );
    }

    private MessageItem toMessageItem(Message message, List<MessageAttachment> attachments) {
        if (message == null) {
            return null;
        }
        List<AttachmentItem> items = attachments == null ? List.of()
                : attachments.stream().map(this::toAttachmentItem).toList();
        return new MessageItem(
                message.getId(),
                message.getSenderId(),
                message.getBody(),
                message.getCreated(),
                message.getEdited(),
                message.getDeleted(),
                items
        );
    }

    private AttachmentItem toAttachmentItem(MessageAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new AttachmentItem(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getSize()
        );
    }

    private Long resolveOtherParticipant(Long conversationId, Long currentId) {
        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversationId);
        return resolveOtherParticipant(participants, currentId);
    }

    private Long resolveOtherParticipant(List<ConversationParticipant> participants, Long currentId) {
        if (participants == null || participants.isEmpty()) {
            return null;
        }
        for (ConversationParticipant participant : participants) {
            Long profileId = participant.getProfileId();
            if (profileId != null && !profileId.equals(currentId)) {
                return profileId;
            }
        }
        return null;
    }

    private String toPairKey(Long firstId, Long secondId) {
        long min = Math.min(firstId, secondId);
        long max = Math.max(firstId, secondId);
        return min + ":" + max;
    }

    private int resolvePageSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, 200);
    }

    private String normalizeBody(String body) {
        if (body == null) {
            return null;
        }
        String trimmed = body.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private List<Long> normalizeAttachmentIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private boolean isAllowedType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        List<String> allowed = attachmentProperties.getAllowedTypes();
        if (allowed == null || allowed.isEmpty()) {
            return false;
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        return allowed.stream().anyMatch(type -> normalized.equals(type.toLowerCase(Locale.ROOT)));
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private void publishAfterCommit(Runnable action) {
        if (action == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    public record CreateConversationRequest(Long otherProfileId) {
    }

    public record ConversationResponse(Long conversationId, Long otherProfileId) {
    }

    public record SendMessageRequest(String body, List<Long> attachmentIds) {
    }

    public record ReadRequest(Long lastReadMessageId) {
    }

    public record ConversationSummary(Long conversationId, Long otherProfileId,
            MessageItem lastMessage, long unreadCount) {
        public Instant lastMessageAt() {
            return lastMessage == null ? null : lastMessage.created();
        }
    }

    public record MessageItem(Long id, Long senderId, String body, Instant created,
            Instant edited, Instant deleted, List<AttachmentItem> attachments) {
    }

    public record AttachmentItem(Long id, String originalName, String contentType, long size) {
    }

    public record ReadEvent(Long conversationId, Long profileId, Long lastReadMessageId) {
    }

    public record UnreadCountResponse(long unreadCount) {
    }
}
