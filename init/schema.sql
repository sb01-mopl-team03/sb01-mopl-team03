-- 사용자 테이블
CREATE TABLE "users"
(
    "id"                       UUID PRIMARY KEY           NOT NULL,
    "email"                    VARCHAR(100) UNIQUE        NOT NULL,
    "name"                     VARCHAR(100) UNIQUE        NOT NULL,
    "password"                 VARCHAR(255)               NOT NULL,
    "created_at"               TIMESTAMP   DEFAULT now()  NOT NULL,
    "updated_at"               TIMESTAMP   DEFAULT now()  NOT NULL,
    "is_locked"                BOOLEAN     DEFAULT false  NOT NULL,
    "role"                     VARCHAR(50) DEFAULT 'USER' NOT NULL,
    "profile_image"            VARCHAR(255)               NULL,
    "is_temp_password"         BOOLEAN     DEFAULT false  NOT NULL,
    "temp_password_expired_at" TIMESTAMP                  NULL
);
COMMENT ON COLUMN "users"."role" IS 'ENUM';

-- 콘텐츠 테이블
CREATE TABLE "contents"
(
    "id"               UUID PRIMARY KEY        NOT NULL,
    "title"            VARCHAR(255)            NOT NULL,
    "title_normalized"            VARCHAR(255)            NOT NULL,
    "data_id"          varchar(255)            NULL,
    "description"      TEXT                    NULL,
    "content_type"     VARCHAR(50)             NOT NULL,
    "release_date"     TIMESTAMP               NOT NULL,
    "avg_rating"       DECIMAL(3, 2)           NULL CHECK (avg_rating >= 0.0 AND avg_rating <= 5.0),
    "created_at"       TIMESTAMP DEFAULT now() NOT NULL,
    "url"              TEXT                    NULL
);
COMMENT ON COLUMN "contents"."content_type" IS 'ENUM';
CREATE INDEX idx_content_title ON contents (title);
CREATE INDEX idx_content_description ON contents (description);

-- 키워드 테이블
CREATE TABLE "keywords"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "user_id"    UUID                    NOT NULL,
    "keyword"    VARCHAR(100)            NOT NULL,
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    FOREIGN KEY ("user_id") REFERENCES "users" ("id")
);

-- 키워드-콘텐츠 관계 테이블
CREATE TABLE "keyword_contents"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "keyword_id" UUID                    NOT NULL,
    "content_id" UUID                    NOT NULL,
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("keyword_id", "content_id"),
    FOREIGN KEY ("keyword_id") REFERENCES "keywords" ("id"),
    FOREIGN KEY ("content_id") REFERENCES "contents" ("id")
);

-- 리뷰 테이블
CREATE TABLE "reviews"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "user_id"    UUID                    NOT NULL,
    "content_id" UUID                    NOT NULL,
    "title"      VARCHAR(255)            NOT NULL,
    "comment"    TEXT                    NULL,
    "rating"     DECIMAL(2, 1)           NOT NULL CHECK (rating >= 0.0 AND rating <= 5.0),
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    "updated_at" TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("user_id", "content_id"),
    FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("content_id") REFERENCES "contents" ("id")
);

-- 플레이리스트 테이블
CREATE TABLE "playlists"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "creator_id" UUID                    NOT NULL,
    "name"       VARCHAR(100)            NULL,
    "is_public"  BOOLEAN                 NOT NULL,
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    "updated_at" TIMESTAMP DEFAULT now() NOT NULL,
    FOREIGN KEY ("creator_id") REFERENCES "users" ("id")
);

-- 플레이리스트-콘텐츠 관계 테이블
CREATE TABLE "playlist_contents"
(
    "id"          UUID PRIMARY KEY        NOT NULL,
    "playlist_id" UUID                    NOT NULL,
    "content_id"  UUID                    NOT NULL,
    "created_at"  TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("playlist_id", "content_id"),
    FOREIGN KEY ("playlist_id") REFERENCES "playlists" ("id"),
    FOREIGN KEY ("content_id") REFERENCES "contents" ("id")
);

-- 구독 테이블
CREATE TABLE "subscriptions"
(
    "id"            UUID PRIMARY KEY        NOT NULL,
    "playlist_id"   UUID                    NOT NULL,
    "subscriber_id" UUID                    NOT NULL,
    "created_at"    TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("playlist_id", "subscriber_id"),
    FOREIGN KEY ("playlist_id") REFERENCES "playlists" ("id"),
    FOREIGN KEY ("subscriber_id") REFERENCES "users" ("id")
);

-- 팔로우 테이블
CREATE TABLE "follows"
(
    "id"           UUID PRIMARY KEY        NOT NULL,
    "follower_id"  UUID                    NOT NULL,
    "following_id" UUID                    NOT NULL,
    "created_at"   TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("follower_id", "following_id"),
    FOREIGN KEY ("follower_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("following_id") REFERENCES "users" ("id")
);

-- 채팅&시청방 테이블
CREATE TABLE "watch_rooms"
(
    "id"                     UUID PRIMARY KEY               NOT NULL,
    "content_id"             UUID                           NOT NULL,
    "owner_id"               UUID                           NOT NULL,
    "created_at"             TIMESTAMP        DEFAULT now() NOT NULL,
    "current_time"           DOUBLE PRECISION DEFAULT 0.0   NOT NULL,
    "is_playing"             BOOLEAN          DEFAULT FALSE NOT NULL,
    "video_state_updated_at" TIMESTAMP        DEFAULT now() NOT NULL,
    FOREIGN KEY ("owner_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("content_id") REFERENCES "contents" ("id")
);

-- 채팅 메시지 테이블
CREATE TABLE "watch_room_messages"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "room_id"    UUID                    NOT NULL,
    "sender_id"  UUID                    NOT NULL,
    "content"    VARCHAR(255)            NOT NULL,
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    FOREIGN KEY ("room_id") REFERENCES "watch_rooms" ("id"),
    FOREIGN KEY ("sender_id") REFERENCES "users" ("id")
);

-- 채팅방 참가자 테이블
CREATE TABLE "watch_room_participants"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "user_id"    UUID                    NOT NULL,
    "room_id"    UUID                    NOT NULL,
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("user_id", "room_id"),
    FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("room_id") REFERENCES "watch_rooms" ("id")
);

-- DM 룸 테이블
CREATE TABLE "dm_rooms"
(
    "id"          UUID PRIMARY KEY        NOT NULL,
    "sender_id"   UUID                    NULL,
    "receiver_id" UUID                    NULL,
    "created_at"  TIMESTAMP DEFAULT now() NOT NULL,
    UNIQUE ("sender_id", "receiver_id"),
    FOREIGN KEY ("sender_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("receiver_id") REFERENCES "users" ("id")
);

-- DM 테이블
CREATE TABLE "dms"
(
    "id"         UUID PRIMARY KEY        NOT NULL,
    "sender_id"  UUID                    NOT NULL,
    "dm_room_id" UUID                    NOT NULL,
    "content"    VARCHAR(255)            NOT NULL,
    "is_read"    BOOLEAN   DEFAULT false NOT NULL,
    "created_at" TIMESTAMP DEFAULT now() NOT NULL,
    FOREIGN KEY ("sender_id") REFERENCES "users" ("id"),
    FOREIGN KEY ("dm_room_id") REFERENCES "dm_rooms" ("id")
);

-- 알림 테이블
CREATE TABLE "notifications"
(
    "id"          UUID PRIMARY KEY        NOT NULL,
    "receiver_id" UUID                    NOT NULL,
    "type"        VARCHAR(255)            NOT NULL,
    "content"     VARCHAR(255)            NOT NULL,
    "is_read"     BOOLEAN                 NOT NULL,
    "created_at"  TIMESTAMP DEFAULT now() NOT NULL,
    FOREIGN KEY ("receiver_id") REFERENCES "users" ("id")
);
COMMENT ON COLUMN "notifications"."type" IS 'ENUM?';


-- JWT 세션 테이블
CREATE TABLE "jwt_sessions"
(
    "id"            UUID PRIMARY KEY        NOT NULL,
    "user_id"       UUID UNIQUE             NOT NULL,
    "access_token"  VARCHAR(512)            NOT NULL,
    "refresh_token" VARCHAR(512)            NOT NULL,
    "created_at"    TIMESTAMP DEFAULT now() NOT NULL,
    "expires_at"    TIMESTAMP               NOT NULL,
    "is_active"     BOOLEAN                 NOT NULL,
    FOREIGN KEY ("user_id") REFERENCES "users" ("id")
);


CREATE TABLE "dm_read_users"
(
    "dm_id"   UUID NOT NULL,
    "user_id" UUID NOT NULL,
    PRIMARY KEY ("dm_id", "user_id"),
    FOREIGN KEY ("dm_id") REFERENCES "dms" ("id")
);

-- 소셜 계정 테이블
-- CREATE TABLE "social_accounts"
-- (
--     "id"        UUID PRIMARY KEY        NOT NULL,
--     "user_id"   UUID                    NOT NULL,
--     "provider"  VARCHAR(50)             NOT NULL,
--     "social_id" UUID                    NOT NULL,
--     "email"     VARCHAR(100)            NULL,
--     "nickname"  VARCHAR(100)            NULL,
--     "linked_at" TEXT                    NULL,
--     FOREIGN KEY ("user_id") REFERENCES "users" ("id")
-- );
-- COMMENT ON COLUMN "social_accounts"."provider" IS 'ENUM';
-- COMMENT ON COLUMN "social_accounts"."social_id" IS 'google/kakao';

