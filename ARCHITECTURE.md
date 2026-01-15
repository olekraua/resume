# Architecture

## Контекст
- Монолітний застосунок на Spring Boot (MVC + Thymeleaf).
- Основна БД: PostgreSQL (JPA/Hibernate).
- Пошук: Elasticsearch (опційно, керується `app.search.elasticsearch.enabled`).
- Зберігання медіа: локальна файлова система (`uploads/...`).
- Асинхронність: внутрішні Spring events після коміту транзакції.

## Потоки

### Редагування профілю
- Вхід: `GET/POST /{uid}/edit/...` через `src/main/java/net/devstudy/resume/controller/EditProfileController.java`.
- Доступ: звірка `uid` з поточним користувачем у `src/main/java/net/devstudy/resume/security/CurrentProfileProvider.java`.
- Валідація: форми в `src/main/java/net/devstudy/resume/form/*` + ручна валідація для `Practic`/`Education`.
- Запис: `src/main/java/net/devstudy/resume/service/impl/ProfileServiceImpl.java` оновлює сутності, прапорець `completed`, публікує подію індексації.

#### Sequence diagram
```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Ctrl as EditProfileController
    participant Auth as CurrentProfileProvider
    participant ProfileSvc as ProfileServiceImpl
    participant Repo as ProfileRepository
    participant Events as ApplicationEventPublisher
    participant Indexer as ProfileSearchIndexingListener
    participant SearchSvc as ProfileSearchService
    participant ES as Elasticsearch

    User->>Browser: Submit edit form
    Browser->>Ctrl: POST /{uid}/edit/...
    Ctrl->>Auth: getCurrentProfile()
    Auth-->>Ctrl: CurrentProfile / null
    alt not authenticated
        Ctrl-->>Browser: redirect /login
    else authenticated
        Ctrl->>ProfileSvc: update...(...)
        ProfileSvc->>Repo: save(...)
        Repo-->>ProfileSvc: saved
        ProfileSvc-->>Ctrl: ok
        ProfileSvc->>Events: publish ProfileIndexingRequestedEvent
        Ctrl-->>Browser: redirect ?success
        Note over Events,Indexer: after commit
        Events-->>Indexer: ProfileIndexingRequestedEvent
        Indexer->>Repo: findById(...)
        Repo-->>Indexer: Profile
        Indexer->>SearchSvc: indexProfiles([Profile])
        SearchSvc->>ES: save document
    end
```

### Пошук
- Вхід: `GET /welcome`, `GET /search` у `src/main/java/net/devstudy/resume/controller/PublicDataController.java`, `GET /api/suggest` у `src/main/java/net/devstudy/resume/controller/SuggestController.java`.
- Запит: `ProfileService.search()` делегує у `ProfileSearchService`.
- Elasticsearch: `src/main/java/net/devstudy/resume/service/impl/ProfileSearchServiceImpl.java` виконує ES‑запит і потім вантажить `Profile` по id з JPA.
- Fallback: при помилках ES повертається JPA‑пошук у `ProfileRepository`.
- Індексація: подія `ProfileIndexingRequestedEvent` → `ProfileSearchIndexingListener` → `ProfileSearchService.indexProfiles()`.

#### Sequence diagram
```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Ctrl as PublicDataController
    participant ProfileSvc as ProfileServiceImpl
    participant SearchSvc as ProfileSearchServiceImpl
    participant ES as Elasticsearch
    participant Repo as ProfileRepository

    User->>Browser: Enter query
    Browser->>Ctrl: GET /search?q=...
    Ctrl->>ProfileSvc: search(q, page)
    ProfileSvc->>SearchSvc: search(q, page)
    alt ES available
        SearchSvc->>ES: query
        ES-->>SearchSvc: hits (ids)
        SearchSvc->>Repo: findAllById(ids)
        Repo-->>SearchSvc: profiles
        SearchSvc-->>ProfileSvc: Page<Profile>
    else ES error
        SearchSvc-->>ProfileSvc: throws
        ProfileSvc->>Repo: search(q, page)
        Repo-->>ProfileSvc: Page<Profile>
    end
    ProfileSvc-->>Ctrl: Page<Profile>
    Ctrl-->>Browser: render results
```

### Відновлення доступу
- Вхід: `GET/POST /restore` та `GET/POST /restore/{token}` у `src/main/java/net/devstudy/resume/controller/RestoreAccessController.java`.
- Запит: `RestoreAccessServiceImpl` знаходить профіль по uid/email/phone, створює токен, хешує і зберігає у `ProfileRestore`.
- Повідомлення: `RestoreAccessMailRequestedEvent` → `RestoreAccessMailListener` → `RestoreAccessMailServiceImpl`.
- Скидання пароля: `resetPassword()` оновлює пароль через `ProfileService`, видаляє токен.

#### Sequence diagram: request restore link
```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Ctrl as RestoreAccessController
    participant RestoreSvc as RestoreAccessServiceImpl
    participant ProfileRepo as ProfileRepository
    participant RestoreRepo as ProfileRestoreRepository
    participant Events as ApplicationEventPublisher
    participant MailListener as RestoreAccessMailListener
    participant MailSvc as RestoreAccessMailServiceImpl
    participant SMTP as SMTP

    User->>Browser: Submit restore form
    Browser->>Ctrl: POST /restore
    Ctrl->>RestoreSvc: requestRestore(identifier, appHost)
    RestoreSvc->>ProfileRepo: findByUid/Email/Phone
    alt profile not found
        ProfileRepo-->>RestoreSvc: empty
        RestoreSvc-->>Ctrl: return link (no mail)
    else profile found
        ProfileRepo-->>RestoreSvc: Profile
        RestoreSvc->>RestoreRepo: save token (hashed)
        RestoreSvc->>Events: publish RestoreAccessMailRequestedEvent
        Note over Events,MailListener: after commit
        Events-->>MailListener: RestoreAccessMailRequestedEvent
        MailListener->>MailSvc: sendRestoreLink(...)
        MailSvc->>SMTP: send
    end
    Ctrl-->>Browser: redirect /restore/success
```

#### Sequence diagram: reset password
```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Ctrl as RestoreAccessController
    participant RestoreSvc as RestoreAccessServiceImpl
    participant RestoreRepo as ProfileRestoreRepository
    participant ProfileSvc as ProfileServiceImpl
    participant ProfileRepo as ProfileRepository

    User->>Browser: Submit new password
    Browser->>Ctrl: POST /restore/{token}
    Ctrl->>RestoreSvc: resetPassword(token, newPassword)
    RestoreSvc->>RestoreRepo: findByToken(hash)
    alt invalid or expired
        RestoreRepo-->>RestoreSvc: empty/expired
        RestoreSvc-->>Ctrl: throws
        Ctrl-->>Browser: redirect /restore?invalid
    else valid
        RestoreRepo-->>RestoreSvc: ProfileRestore
        RestoreSvc->>ProfileSvc: updatePassword(profileId, rawPassword)
        ProfileSvc->>ProfileRepo: save
        RestoreSvc->>RestoreRepo: delete token
        Ctrl-->>Browser: redirect /login?restored
    end
```

### Медіа (фото/сертифікати)
- Вхід: `POST /{uid}/edit/photo` і `POST /{uid}/edit/certificates/upload` у `EditProfileController`.
- Обробка: валідація, конвертація, ресайз, оптимізація у `PhotoStorageServiceImpl` та `CertificateStorageServiceImpl`.
- Збереження: файли у `uploads/...`, повернення URL для збереження в профілі.
- Очищення: `PhotoFileStorage` та `CertificateFileStorage` видаляють старі файли після коміту.

#### Sequence diagram: photo upload
```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Ctrl as EditProfileController
    participant PhotoSvc as PhotoStorageServiceImpl
    participant Temp as UploadImageTempStorage
    participant Resizer as ImageResizer
    participant Optim as ImageOptimizator
    participant FS as Filesystem
    participant ProfileSvc as ProfileServiceImpl
    participant ProfileRepo as ProfileRepository

    User->>Browser: Select photo
    Browser->>Ctrl: POST /{uid}/edit/photo
    Ctrl->>PhotoSvc: store(file)
    PhotoSvc->>Temp: getCurrentUploadTempPath()
    PhotoSvc->>Resizer: resize
    PhotoSvc->>Optim: optimize
    PhotoSvc->>FS: write uploads/photos
    PhotoSvc-->>Ctrl: [largeUrl, smallUrl]
    Ctrl->>ProfileSvc: updatePhoto(profileId, urls)
    ProfileSvc->>ProfileRepo: save
    Ctrl-->>Browser: redirect ?success
```

#### Sequence diagram: certificate upload + save
```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Ctrl as EditProfileController
    participant CertSvc as CertificateStorageServiceImpl
    participant Temp as UploadImageTempStorage
    participant Resizer as ImageResizer
    participant Optim as ImageOptimizator
    participant FS as Filesystem
    participant LinkTemp as UploadCertificateLinkTempStorage
    participant ProfileSvc as ProfileServiceImpl
    participant CertRepo as CertificateRepository
    participant FileStore as CertificateFileStorage

    User->>Browser: Upload certificate image
    Browser->>Ctrl: POST /{uid}/edit/certificates/upload
    Ctrl->>CertSvc: store(file)
    CertSvc->>Temp: getCurrentUploadTempPath()
    CertSvc->>Resizer: resize
    CertSvc->>Optim: optimize
    CertSvc->>FS: write uploads/certificates
    CertSvc->>LinkTemp: addImageLinks(largeUrl, smallUrl)
    CertSvc-->>Ctrl: UploadCertificateResult

    User->>Browser: Submit certificates form
    Browser->>Ctrl: POST /{uid}/edit/certificates
    Ctrl->>ProfileSvc: updateCertificates(profileId, items)
    ProfileSvc->>CertRepo: delete/save
    Note over ProfileSvc,FileStore: after commit
    ProfileSvc->>LinkTemp: clearImageLinks()
    ProfileSvc->>FileStore: removeAll(oldUrls)
    Ctrl-->>Browser: redirect ?success
```

## Власники даних (Source of Truth)

### Profile domain
- `Profile`, `Contacts`, `Skill`, `Practic`, `Education`, `Course`, `Language`, `Certificate` у `src/main/java/net/devstudy/resume/entity/*`.
- Власник логіки оновлення: `src/main/java/net/devstudy/resume/service/impl/ProfileServiceImpl.java`.

### StaticData domain
- `SkillCategory`, `Hobby` у `src/main/java/net/devstudy/resume/entity/SkillCategory.java` та `src/main/java/net/devstudy/resume/entity/Hobby.java`.
- Читання: `src/main/java/net/devstudy/resume/service/impl/StaticDataServiceImpl.java`.

### Auth domain
- `ProfileRestore` у `src/main/java/net/devstudy/resume/entity/ProfileRestore.java`.
- Логіка: `src/main/java/net/devstudy/resume/service/impl/RestoreAccessServiceImpl.java`.

### Search domain (похідні дані)
- `ProfileSearchDocument` у `src/main/java/net/devstudy/resume/search/ProfileSearchDocument.java`.
- Дані індексуються з профілю, не є джерелом істини.

### Media domain
- Файли у файловій системі (`uploads/photos`, `uploads/certificates`).
- URL‑посилання зберігаються у `Profile`/`Certificate`.

## Інтеграції та інфраструктура
- PostgreSQL для основних даних.
- Elasticsearch для пошуку (опційно).
- SMTP для відновлення доступу.

## Модульні межі й залежності

### Пропоновані модулі
- `profile`: профільні сутності, оновлення, completed‑логіка.
- `staticdata`: довідники (хобі, категорії навичок, мапи років/місяців).
- `auth`: автентифікація, відновлення доступу, обмеження доступу.
- `search`: індексація та пошук (ES), похідні документи.
- `media`: обробка/зберігання фото й сертифікатів.
- `notification`: email‑відновлення.
- `shared`: спільні типи, валідації, утиліти.
- `web`: MVC‑контролери, UI‑конфіги, шаблони.

### Дозволені залежності (на рівні модулів)
- `web` → `profile`, `staticdata`, `auth`, `search`, `media`, `notification`, `shared`
- `profile` → `staticdata`, `search`, `media`, `shared`
- `auth` → `profile`, `notification`, `shared`
- `search` → `profile`, `shared`
- `media` → `shared`
- `notification` → `shared`
- `staticdata` → `shared`
- `shared` → (немає)

### Принципи
- Зовнішні модулі не звертаються напряму до репозиторіїв іншого модуля.
- Доступ між модулями лише через інтерфейси сервісів або події.
- `search` і `notification` оперують лише даними, переданими у подіях/DTO.

## Package mapping (target)

### app
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.app` | `ResumeApplication` |
| `net.devstudy.resume.app.config` | `AppInfoConfig`, `RepositoryConfig` |

### web
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.web.controller.auth` | `AuthController`, `AccountController`, `RestoreAccessController` |
| `net.devstudy.resume.web.controller.profile` | `EditProfileController`, `EditShortcutController` |
| `net.devstudy.resume.web.controller.public` | `PublicDataController` |
| `net.devstudy.resume.web.controller.search` | `SuggestController` |
| `net.devstudy.resume.web.advice` | `GlobalExceptionHandler` |
| `net.devstudy.resume.web.config` | `UiProperties`, `UiModelAttributes`, `UploadResourceConfig` |
| `net.devstudy.resume.web.filter` | `AbstractFilter` |

### profile
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.profile.entity` | `AbstractEntity`, `AbstractFinishDateEntity`, `Profile`, `Contacts`, `Skill`, `Practic`, `Education`, `Course`, `Language`, `Certificate`, `ProfileEntity`, `ProfileCollectionField` |
| `net.devstudy.resume.profile.repository` | `ProfileRepository`, `SkillRepository`, `PracticRepository`, `EducationRepository`, `CourseRepository`, `LanguageRepository`, `CertificateRepository` |
| `net.devstudy.resume.profile.service` | `ProfileService`, `ProfileServiceImpl` |
| `net.devstudy.resume.profile.form` | `ProfileMainForm`, `InfoForm`, `ContactsForm`, `SkillForm`, `PracticForm`, `EducationForm`, `CourseForm`, `LanguageForm`, `CertificateForm`, `HobbyForm` |
| `net.devstudy.resume.profile.model` | `LanguageType`, `LanguageLevel` |
| `net.devstudy.resume.profile.annotation` | `ProfileInfoField`, `ProfileDataFieldGroup` |
| `net.devstudy.resume.profile.exception` | `UidAlreadyExistsException` |

### staticdata
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.staticdata.entity` | `SkillCategory`, `Hobby` |
| `net.devstudy.resume.staticdata.repository` | `SkillCategoryRepository`, `HobbyRepository` |
| `net.devstudy.resume.staticdata.service` | `StaticDataService`, `StaticDataServiceImpl` |

### auth
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.auth.config` | `SecurityConfig` |
| `net.devstudy.resume.auth.security` | `CurrentProfileProvider`, `SecurityContextCurrentProfileProvider`, `AccessDeniedHandlerImpl`, `CurrentProfileDetailsService` |
| `net.devstudy.resume.auth.entity` | `ProfileRestore`, `RememberMeToken` |
| `net.devstudy.resume.auth.repository` | `ProfileRestoreRepository`, `RememberMeTokenRepository` |
| `net.devstudy.resume.auth.service` | `RestoreAccessService`, `RestoreAccessServiceImpl`, `UidSuggestionService`, `UidSuggestionServiceImpl`, `RememberMeService` |
| `net.devstudy.resume.auth.model` | `CurrentProfile`, `CurrentProfileImpl` |
| `net.devstudy.resume.auth.form` | `RegistrationForm`, `ChangeLoginForm`, `ChangePasswordForm`, `RestoreAccessForm`, `RestorePasswordForm`, `PasswordForm`, `SignUpForm` |
| `net.devstudy.resume.auth.util` | `SecurityUtil` |

### search
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.search.document` | `ProfileSearchDocument` |
| `net.devstudy.resume.search.repository` | `ProfileSearchRepository` |
| `net.devstudy.resume.search.service` | `ProfileSearchService`, `ProfileSearchServiceImpl`, `ProfileSearchServiceNoOp` |
| `net.devstudy.resume.search.mapper` | `ProfileSearchMapper`, `ProfileSearchMapperImpl` |
| `net.devstudy.resume.search.listener` | `ProfileSearchIndexingListener` |
| `net.devstudy.resume.search.event` | `ProfileIndexingRequestedEvent` |
| `net.devstudy.resume.search.config` | `ElasticsearchIndexConfig`, `ElasticsearchRepositoryConfig` |

### media
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.media.model` | `UploadTempPath`, `UploadCertificateResult` |
| `net.devstudy.resume.media.annotation` | `EnableUploadImageTempStorage` |
| `net.devstudy.resume.media.component` | `ImageResizer`, `ImageOptimizator`, `ImageFormatConverter`, `UploadTempPathFactory`, `PhotoFileStorage`, `CertificateFileStorage` |
| `net.devstudy.resume.media.component.impl` | `ThumbnailsImageResizer`, `JpegTranImageOptimizator`, `PngToJpegImageFormatConverter`, `DefaultUploadTempPathFactory`, `UploadImageTempStorage`, `UploadCertificateLinkTempStorage` |
| `net.devstudy.resume.media.service` | `PhotoStorageService`, `PhotoStorageServiceImpl`, `CertificateStorageService`, `CertificateStorageServiceImpl` |
| `net.devstudy.resume.media.config` | `PhotoUploadProperties`, `CertificateUploadProperties` |

### notification
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.notification.event` | `RestoreAccessMailRequestedEvent` |
| `net.devstudy.resume.notification.listener` | `RestoreAccessMailListener` |
| `net.devstudy.resume.notification.service` | `RestoreAccessMailService`, `RestoreAccessMailServiceImpl`, `RestoreAccessMailServiceNoOp` |
| `net.devstudy.resume.notification.template` | `TemplateResolver`, `FreemarkerTemplateResolver` |
| `net.devstudy.resume.notification.config` | `RestoreMailTemplateProperties` |

### shared
| Пакет | Класи |
| --- | --- |
| `net.devstudy.resume.shared.constants` | `Constants` |
| `net.devstudy.resume.shared.model` | `AbstractModel` |
| `net.devstudy.resume.shared.util` | `BeanCopyUtil`, `DataUtil`, `SanitizationUtils` |
| `net.devstudy.resume.shared.component` | `DataBuilder`, `TranslitConverter`, `FormErrorConverter` |
| `net.devstudy.resume.shared.component.impl` | `DataBuilderImpl`, `SimpleTranslitConverter`, `JunidecodeTranslitConverter` |
| `net.devstudy.resume.shared.annotation` | `EnableFormErrorConversion` |
| `net.devstudy.resume.shared.validation.annotation` | `Adulthood`, `EnglishLanguage`, `FieldMatch`, `FirstFieldLessThanSecond`, `MinDigitCount`, `MinLowerCharCount`, `MinSpecCharCount`, `MinUpperCharCount`, `PasswordStrength`, `PasswordsMatch`, `Phone`, `RestoreIdentifier` |
| `net.devstudy.resume.shared.validation.validator` | `AdulthoodConstraintValidator`, `EnglishLanguageConstraintValidator`, `FieldMatchConstraintValidator`, `FirstFieldLessThanSecondConstraintValidator`, `MinDigitCountConstraintValidator`, `MinLowerCharCountConstraintValidator`, `MinSpecCharCountConstraintValidator`, `MinUpperCharCountConstraintValidator`, `PasswordsMatchValidator`, `PhoneConstraintValidator`, `RestoreIdentifierConstraintValidator`, `HtmlSanitized`, `HtmlSanitizedValidator` |
