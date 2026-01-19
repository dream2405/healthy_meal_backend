LOAD DATA LOCAL INFILE 'src/main/resources/db/migration/mysql/csv/preprocessedFood_3_ordered.csv'
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
    @name,
    @representative_food,
    @major_category,
    @weight,
    @energy_kcal,
    @carbohydrate_g,
    @protein_g,
    @calcium_mg,
    @kalium_mg,
    @iron_mg,
    @magnesium_mg,
    @zinc_mg,
    @cellulose_g,
    @aminoacid_mg,
    @leucine_mg,
    @methionine_mg,
    @selenium_ug,
    @omega3_g,
    @vitaminA_ug,
    @vitaminB_mg,
    @folicacid_ug,
    @vitaminB12_mg,
    @vitaminC_mg,
    @vitaminD_ug,
    @vitaminE_mg
)
-- 2. 변수 값을 실제 테이블 컬럼에 SET 합니다.
--    이때 NULLIF 함수를 사용해, 빈 문자열('')이면 NULL로 변환합니다.
SET
    name = NULLIF(@name, ''),
    representative_food = NULLIF(@representative_food, ''),
    major_category = NULLIF(@major_category, ''),
    weight = NULLIF(@weight, ''),
    energy_kcal = NULLIF(@energy_kcal, ''),
    carbohydrate_g = NULLIF(@carbohydrate_g, ''),
    protein_g = NULLIF(@protein_g, ''),
    calcium_mg = NULLIF(@calcium_mg, ''),
    kalium_mg = NULLIF(@kalium_mg, ''),
    iron_mg = NULLIF(@iron_mg, ''),
    magnesium_mg = NULLIF(@magnesium_mg, ''),
    zinc_mg = NULLIF(@zinc_mg, ''),
    cellulose_g = NULLIF(@cellulose_g, ''),
    aminoacid_mg = NULLIF(@aminoacid_mg, ''),
    leucine_mg = NULLIF(@leucine_mg, ''),
    methionine_mg = NULLIF(@methionine_mg, ''),
    selenium_ug = NULLIF(@selenium_ug, ''),
    omega3_g = NULLIF(@omega3_g, ''),
    vitaminA_ug = NULLIF(@vitaminA_ug, ''),
    vitaminB_mg = NULLIF(@vitaminB_mg, ''),
    folicacid_ug = NULLIF(@folicacid_ug, ''),
    vitaminB12_mg = NULLIF(@vitaminB12_mg, ''),
    vitaminC_mg = NULLIF(@vitaminC_mg, ''),
    vitaminD_ug = NULLIF(@vitaminD_ug, ''),
    vitaminE_mg = NULLIF(@vitaminE_mg, '');