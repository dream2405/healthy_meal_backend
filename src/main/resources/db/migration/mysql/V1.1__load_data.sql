LOAD DATA LOCAL INFILE 'src/main/resources/db/migration/mysql/csv/V1.1__food.csv'
INTO TABLE food
CHARACTER SET 'utf8mb4' -- 한글 데이터(예: "빵 및 과자류")를 위해 utf8mb4 권장
FIELDS
    TERMINATED BY ','
    OPTIONALLY ENCLOSED BY '"' -- 필드가 따옴표로 감싸져 있을 수도 있음
LINES
    TERMINATED BY '\n' -- 표준 줄바꿈
IGNORE 1 LINES -- CSV의 첫 번째 줄(헤더) 무시

-- 1. CSV의 모든 열을 순서대로 사용자 변수(@)로 읽어들입니다.
(
    @carbohydrate_g,
    @cellulose_g,
    @cholesterol_mg,
    @energy_kcal,
    @fat_g,
    @protein_g,
    @sodium_mg,
    @sugars_g,
    @nutri_ref_amt,
    @id,
    @major_category,
    @medium_category,
    @minor_category,
    @name,
    @representative_food,
    @subcategory,
    @weight
)
-- 2. 변수 값을 실제 테이블 컬럼에 SET 합니다.
--    이때 NULLIF 함수를 사용해, 빈 문자열('')이면 NULL로 변환합니다.
SET
    carbohydrate_g = NULLIF(@carbohydrate_g, ''),
    cellulose_g = NULLIF(@cellulose_g, ''),
    cholesterol_mg = NULLIF(@cholesterol_mg, ''),
    energy_kcal = NULLIF(@energy_kcal, ''),
    fat_g = NULLIF(@fat_g, ''),
    protein_g = NULLIF(@protein_g, ''),
    sodium_mg = NULLIF(@sodium_mg, ''),
    sugars_g = NULLIF(@sugars_g, ''),
    nutri_ref_amt = NULLIF(@nutri_ref_amt, ''),
    id = NULLIF(@id, ''),
    major_category = NULLIF(@major_category, ''),
    medium_category = NULLIF(@medium_category, ''),
    minor_category = NULLIF(@minor_category, ''),
    name = NULLIF(@name, ''),
    representative_food = NULLIF(@representative_food, ''),
    subcategory = NULLIF(@subcategory, ''),
    weight = NULLIF(@weight, '');