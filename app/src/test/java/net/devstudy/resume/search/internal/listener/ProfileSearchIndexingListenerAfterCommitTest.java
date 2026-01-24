package net.devstudy.resume.search.internal.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import net.devstudy.resume.profile.api.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.profile.api.event.ProfileIndexingSnapshot;
import net.devstudy.resume.profile.api.service.ProfileSearchService;

class ProfileSearchIndexingListenerAfterCommitTest {

    private static final class SimpleTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
            // no-op
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
            // no-op
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
            // no-op
        }
    }

    @Test
    void indexesOnlyAfterCommit() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        ProfileIndexingSnapshot snapshot = new ProfileIndexingSnapshot(1L, "uid", "first", "last",
                "objective", "summary", "info", List.of());

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ProfileSearchService.class, () -> profileSearchService);
            context.registerBean(PlatformTransactionManager.class, SimpleTransactionManager::new);
            context.registerBean(TransactionalEventListenerFactory.class);
            context.registerBean(ProfileSearchIndexingListener.class,
                    () -> new ProfileSearchIndexingListener(profileSearchService));
            context.refresh();

            TransactionTemplate transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            transaction.executeWithoutResult(status -> {
                context.publishEvent(new ProfileIndexingRequestedEvent(snapshot));
                verifyNoInteractions(profileSearchService);
            });

            verify(profileSearchService).indexProfiles(anyList());
        }
    }

    @Test
    void doesNotIndexOnRollback() {
        ProfileSearchService profileSearchService = mock(ProfileSearchService.class);
        ProfileIndexingSnapshot snapshot = new ProfileIndexingSnapshot(1L, "uid", "first", "last",
                "objective", "summary", "info", List.of());

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ProfileSearchService.class, () -> profileSearchService);
            context.registerBean(PlatformTransactionManager.class, SimpleTransactionManager::new);
            context.registerBean(TransactionalEventListenerFactory.class);
            context.registerBean(ProfileSearchIndexingListener.class,
                    () -> new ProfileSearchIndexingListener(profileSearchService));
            context.refresh();

            TransactionTemplate transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(status -> {
                context.publishEvent(new ProfileIndexingRequestedEvent(snapshot));
                throw new RuntimeException("boom");
            }));

            verifyNoInteractions(profileSearchService);
        }
    }
}
