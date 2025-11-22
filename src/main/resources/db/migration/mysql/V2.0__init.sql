CREATE TABLE diet_criterion
(
    id             INT AUTO_INCREMENT NOT NULL,
    start_age      INT                NULL,
    end_age        INT                NULL,
    gender         CHAR               NULL,
    energy_kcal    FLOAT              NULL,
    carbohydrate_g FLOAT              NULL,
    protein_g      FLOAT              NULL,
    calcium_mg     FLOAT              NULL,
    kalium_mg      FLOAT              NULL,
    iron_mg        FLOAT              NULL,
    magnesium_mg   FLOAT              NULL,
    zinc_mg        FLOAT              NULL,
    cellulose_g    FLOAT              NULL,
    aminoacid_mg   FLOAT              NULL,
    leucine_mg     FLOAT              NULL,
    methionine_mg  FLOAT              NULL,
    selenium_ug    FLOAT              NULL,
    omega3_g       FLOAT              NULL,
    vitaminA_ug    FLOAT              NULL,
    vitaminB_mg    FLOAT              NULL,
    folicacid_ug   FLOAT              NULL,
    vitaminB12_mg  FLOAT              NULL,
    vitaminC_mg    FLOAT              NULL,
    vitaminD_ug    FLOAT              NULL,
    vitaminE_mg    FLOAT              NULL,
    CONSTRAINT pk_diet_criterion PRIMARY KEY (id)
);

CREATE TABLE food
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    name                VARCHAR(255)          NOT NULL,
    representative_food VARCHAR(255)          NULL,
    major_category      VARCHAR(255)          NULL,
    weight              VARCHAR(255)          NULL,
    energy_kcal         DOUBLE                 NULL,
    carbohydrate_g      DOUBLE                 NULL,
    protein_g           DOUBLE                 NULL,
    calcium_mg          DOUBLE                 NULL,
    kalium_mg           DOUBLE                 NULL,
    iron_mg             DOUBLE                 NULL,
    magnesium_mg        DOUBLE                 NULL,
    zinc_mg             DOUBLE                 NULL,
    cellulose_g         DOUBLE                 NULL,
    aminoacid_mg        DOUBLE                 NULL,
    leucine_mg          DOUBLE                 NULL,
    methionine_mg       DOUBLE                 NULL,
    selenium_ug         DOUBLE                 NULL,
    omega3_g            DOUBLE                 NULL,
    vitaminA_ug         DOUBLE                 NULL,
    vitaminB_mg         DOUBLE                 NULL,
    folicacid_ug        DOUBLE                 NULL,
    vitaminB12_mg       DOUBLE                 NULL,
    vitaminC_mg         DOUBLE                 NULL,
    vitaminD_ug         DOUBLE                 NULL,
    vitaminE_mg         DOUBLE                 NULL,
    CONSTRAINT pk_food PRIMARY KEY (id)
);

CREATE TABLE user
(
    id                  VARCHAR(255) NOT NULL,
    hashed_password     VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    birthday            date         NOT NULL,
    gender              CHAR         NOT NULL,
    nutrient_modelname  VARCHAR(255) NOT NULL,
    nutrition_level     FLOAT        NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE daily_intake
(
    id                  INT AUTO_INCREMENT      NOT NULL,
    day                 date                    NULL,
    energy_kcal         DOUBLE                  NULL,
    carbohydrate_g      DOUBLE                  NULL,
    protein_g           DOUBLE                  NULL,
    calcium_mg          DOUBLE                  NULL,
    kalium_mg           DOUBLE                  NULL,
    iron_mg             DOUBLE                  NULL,
    magnesium_mg        DOUBLE                  NULL,
    zinc_mg             DOUBLE                  NULL,
    cellulose_g         DOUBLE                  NULL,
    aminoacid_mg        DOUBLE                  NULL,
    leucine_mg          DOUBLE                  NULL,
    methionine_mg       DOUBLE                  NULL,
    selenium_ug         DOUBLE                  NULL,
    omega3_g            DOUBLE                  NULL,
    vitaminA_ug         DOUBLE                  NULL,
    vitaminB_mg         DOUBLE                  NULL,
    folicacid_ug        DOUBLE                  NULL,
    vitaminB12_mg       DOUBLE                  NULL,
    vitaminC_mg         DOUBLE                  NULL,
    vitaminD_ug         DOUBLE                  NULL,
    vitaminE_mg         DOUBLE                  NULL,
    user_id             VARCHAR(255)            NOT NULL,
    CONSTRAINT pk_daily_intake PRIMARY KEY (id)
);
# 한 사람당 하루에 하나의 daily_intake만 가짐
ALTER TABLE daily_intake
    ADD CONSTRAINT uc_ad6b26af46cf9d9a03ca385db UNIQUE (user_id, day);
ALTER TABLE daily_intake
    ADD CONSTRAINT FK_DAILY_INTAKE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE TABLE mealrecord
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    img_path         VARCHAR(255)          NULL,
    meal_name        VARCHAR(255)          NULL,
    diary            TINYTEXT              NULL,
    taken_at         datetime              NULL,
    created_at       datetime              NULL,
    last_modified_at datetime              NULL,
    user_id          VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_mealrecord PRIMARY KEY (id)
);
ALTER TABLE mealrecord
    ADD CONSTRAINT FK_MEALRECORD_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE TABLE mealrecord_food_link
(
    intake_amount FLOAT  NULL,
    mealrecord_id  BIGINT NOT NULL,
    food_id       BIGINT NOT NULL,
    CONSTRAINT pk_mealrecord_food_link PRIMARY KEY (mealrecord_id, food_id)
);
ALTER TABLE mealrecord_food_link
    ADD CONSTRAINT FK_MEALRECORD_FOOD_LINK_ON_FOOD FOREIGN KEY (food_id) REFERENCES food (id) ON DELETE CASCADE;
ALTER TABLE mealrecord_food_link
    ADD CONSTRAINT FK_MEALRECORD_FOOD_LINK_ON_MEALRECORD FOREIGN KEY (mealrecord_id) REFERENCES mealrecord (id) ON DELETE CASCADE;

CREATE TABLE nutrient_weight
(
    id              INT AUTO_INCREMENT NOT NULL,
    nutrient_name   VARCHAR(255)       NOT NULL,
    weight          FLOAT              NOT NULL,
    user_id         VARCHAR(255)       NOT NULL,
    CONSTRAINT pk_nutrient_weight PRIMARY KEY (id)
);
ALTER TABLE nutrient_weight
    ADD CONSTRAINT FK_NUTRIENT_WEIGHT_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

CREATE TABLE user_food_link
(
    food_id     BIGINT       NOT NULL,
    user_id     VARCHAR(255) NOT NULL,
    preference  DOUBLE       NULL,
    CONSTRAINT pk_user_food_link PRIMARY KEY (food_id, user_id)
);
ALTER TABLE user_food_link
    ADD CONSTRAINT FK_USER_FOOD_LINK_ON_FOOD FOREIGN KEY (food_id) REFERENCES food (id) ON DELETE CASCADE;
ALTER TABLE user_food_link
    ADD CONSTRAINT FK_USER_FOOD_LINK_ON_USER FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE;

