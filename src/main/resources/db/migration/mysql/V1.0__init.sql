CREATE TABLE diet_criterion
(
    id             INT AUTO_INCREMENT NOT NULL,
    start_age      INT                NULL,
    end_age        INT                NULL,
    gender         CHAR               NULL,
    energy_kcal    FLOAT              NULL,
    protein_g      FLOAT              NULL,
    fat_g          FLOAT              NULL,
    carbohydrate_g FLOAT              NULL,
    sugars_g       FLOAT              NULL,
    cellulose_g    FLOAT              NULL,
    sodium_mg      FLOAT              NULL,
    cholesterol_mg FLOAT              NULL,
    CONSTRAINT pk_diet_criterion PRIMARY KEY (id)
);

CREATE TABLE diet_scoring_criterion
(
    id                        BIGINT AUTO_INCREMENT NOT NULL,
    nutrient_name             VARCHAR(255)          NOT NULL,
    recommended_amount        DOUBLE                NULL,
    min_optimal_ratio         DOUBLE                NULL,
    max_optimal_ratio         DOUBLE                NULL,
    penalty_start_ratio_upper DOUBLE                NULL,
    zero_score_ratio_upper    DOUBLE                NULL,
    CONSTRAINT pk_diet_scoring_criterion PRIMARY KEY (id)
);

ALTER TABLE diet_scoring_criterion
    ADD CONSTRAINT uc_diet_scoring_criterion_nutrient_name UNIQUE (nutrient_name);

CREATE TABLE food
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    name                VARCHAR(255)          NOT NULL,
    representative_food VARCHAR(255)          NULL,
    major_category      VARCHAR(255)          NULL,
    medium_category     VARCHAR(255)          NULL,
    minor_category      VARCHAR(255)          NULL,
    subcategory         VARCHAR(255)          NULL,
    nutri_ref_amt       VARCHAR(5)            NULL,
    weight              VARCHAR(255)          NULL,
    energy_kcal         DOUBLE                NULL,
    protein_g           DOUBLE                NULL,
    fat_g               DOUBLE                NULL,
    carbohydrate_g      DOUBLE                NULL,
    sugars_g            DOUBLE                NULL,
    cellulose_g         DOUBLE                NULL,
    sodium_mg           DOUBLE                NULL,
    cholesterol_mg      DOUBLE                NULL,
    CONSTRAINT pk_food PRIMARY KEY (id)
);

CREATE TABLE food_cluster
(
    cluster_num    BIGINT AUTO_INCREMENT NOT NULL,
    `description`  VARCHAR(255)          NULL,
    high_nutrients VARCHAR(255)          NULL,
    low_nutrients  VARCHAR(255)          NULL,
    nutri_vector   VARCHAR(255)          NULL,
    CONSTRAINT pk_foodcluster PRIMARY KEY (cluster_num)
);

CREATE TABLE food_recommendation
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    category       VARCHAR(255)          NULL,
    energy_kcal    DOUBLE                NULL,
    protein_g      DOUBLE                NULL,
    fat_g          DOUBLE                NULL,
    carbohydrate_g DOUBLE                NULL,
    sugars_g       DOUBLE                NULL,
    sodium_mg      DOUBLE                NULL,
    cholesterol_mg DOUBLE                NULL,
    cluster_num    BIGINT                NULL,
    CONSTRAINT pk_foodrecommendation PRIMARY KEY (id)
);

ALTER TABLE food_recommendation
    ADD CONSTRAINT FK_FOODRECOMMENDATION_ON_CLUSTER_NUM FOREIGN KEY (cluster_num) REFERENCES food_cluster (cluster_num);

CREATE TABLE user
(
    id              VARCHAR(255) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    birthday        date         NULL,
    gender          CHAR         NULL,
    critweight      VARCHAR(255) NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE daily_intake
(
    id             INT AUTO_INCREMENT NOT NULL,
    day            date               NULL,
    energy_kcal    DOUBLE             NULL,
    protein_g      DOUBLE             NULL,
    fat_g          DOUBLE             NULL,
    carbohydrate_g DOUBLE             NULL,
    sugars_g       DOUBLE             NULL,
    cellulose_g    DOUBLE             NULL,
    sodium_mg      DOUBLE             NULL,
    cholesterol_mg DOUBLE             NULL,
    score          INT                NULL,
    user_id        VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_daily_intake PRIMARY KEY (id)
);

ALTER TABLE daily_intake
    ADD CONSTRAINT uc_ad6b26af46cf9d9a03ca385db UNIQUE (user_id, day);

ALTER TABLE daily_intake
    ADD CONSTRAINT FK_DAILY_INTAKE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE TABLE meal_info
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    img_path         VARCHAR(255)          NULL,
    created_at       datetime              NULL,
    last_modified_at datetime              NULL,
    diary            TINYTEXT              NULL,
    user_id          VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_meal_info PRIMARY KEY (id)
);

ALTER TABLE meal_info
    ADD CONSTRAINT FK_MEAL_INFO_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE TABLE meal_info_food_link
(
    intake_amount FLOAT  NULL,
    meal_info_id  BIGINT NOT NULL,
    food_id       BIGINT NOT NULL,
    CONSTRAINT pk_meal_info_food_link PRIMARY KEY (meal_info_id, food_id)
);

ALTER TABLE meal_info_food_link
    ADD CONSTRAINT FK_MEAL_INFO_FOOD_LINK_ON_FOOD FOREIGN KEY (food_id) REFERENCES food (id) ON DELETE CASCADE;

ALTER TABLE meal_info_food_link
    ADD CONSTRAINT FK_MEAL_INFO_FOOD_LINK_ON_MEAL_INFO FOREIGN KEY (meal_info_id) REFERENCES meal_info (id) ON DELETE CASCADE;

CREATE TABLE nutri_weight
(
    id       INT AUTO_INCREMENT NOT NULL,
    nutrient VARCHAR(255)       NULL,
    weight   FLOAT              NULL,
    user_id  VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_nutri_weight PRIMARY KEY (id)
);

ALTER TABLE nutri_weight
    ADD CONSTRAINT FK_NUTRI_WEIGHT_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE TABLE user_food_link
(
    food_id BIGINT       NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_food_link PRIMARY KEY (food_id, user_id)
);

ALTER TABLE user_food_link
    ADD CONSTRAINT FK_USER_FOOD_LINK_ON_FOOD FOREIGN KEY (food_id) REFERENCES food (id) ON DELETE CASCADE;

ALTER TABLE user_food_link
    ADD CONSTRAINT FK_USER_FOOD_LINK_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

