package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.lang.reflect.Field;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "daily_intake", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day"}))
public class DailyIntake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "day")
    private LocalDate day;

    @Column(name = "dailyscore")
    private Integer dailyscore;

    @Column(name = "energy_kcal")
    private Double energyKcal;

    @Column(name = "carbohydrate_g")
    private Double carbohydrateG;

    @Column(name = "protein_g")
    private Double proteinG;

    @Column(name = "calcium_mg")
    private Double calciumMg;

    @Column(name = "kalium_mg")
    private Double kaliumMg;

    @Column(name = "iron_mg")
    private Double ironMg;

    @Column(name = "magnesium_mg")
    private Double magnesiumMg;

    @Column(name = "zinc_mg")
    private Double zincMg;

    @Column(name = "cellulose_g")
    private Double celluloseG;

    @Column(name = "aminoacid_mg")
    private Double aminoacidMg;

    @Column(name = "leucine_mg")
    private Double leucineMg;

    @Column(name = "methionine_mg")
    private Double methionineMg;

    @Column(name = "selenium_ug")
    private Double seleniumUg;

    @Column(name = "omega3_g")
    private Double omega3G;

    @Column(name = "vitaminA_ug")
    private Double vitaminAUg;

    @Column(name = "vitaminB_mg")
    private Double vitaminBMg;

    @Column(name = "folicacid_ug")
    private Double folicacidUg;

    @Column(name = "vitaminB12_ug")
    private Double vitaminB12Ug;

    @Column(name = "vitaminC_mg")
    private Double vitaminCMg;

    @Column(name = "vitaminD_ug")
    private Double vitaminDUg;

    @Column(name = "vitaminE_mg")
    private Double vitaminEMg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 일별 섭취 기록을 기록한 유저

    public Double getValue(String fieldName) {
        switch (fieldName) {
            case "energyKcal": return energyKcal;
            case "carbohydrateG": return carbohydrateG;
            case "proteinG": return proteinG;
            case "calciumMg": return calciumMg;
            case "kaliumMg": return kaliumMg;
            case "ironMg": return ironMg;
            case "magnesiumMg": return magnesiumMg;
            case "zincMg": return zincMg;
            case "celluloseG": return celluloseG;
            case "aminoacidMg": return aminoacidMg;
            case "leucineMg": return leucineMg;
            case "methionineMg": return methionineMg;
            case "seleniumUg": return seleniumUg;
            case "omega3G": return omega3G;
            case "vitaminAUg": return vitaminAUg;
            case "vitaminBMg": return vitaminBMg;
            case "folicacidUg": return folicacidUg;
            case "vitaminB12Ug": return vitaminB12Ug;
            case "vitaminCMg": return vitaminCMg;
            case "vitaminDUg": return vitaminDUg;
            case "vitaminEMg": return vitaminEMg;
            default:
                throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
    }



    public void addMealIntake(double energyKcal, double carbohydrateG, double proteinG,
                              double calciumMg, double kaliumMg, double ironMg, double magnesiumMg, double zincMg,
                              double celluloseG, double aminoacidMg, double leucineMg, double methionineMg,
                              double seleniumUg, double omega3G, double vitaminAUg, double vitaminBMg,
                              double folicacidUg, double vitaminB12Ug, double vitaminCMg, double vitaminDUg, double vitaminEMg) {

        this.energyKcal = safeAdd(this.energyKcal, energyKcal);
        this.carbohydrateG = safeAdd(this.carbohydrateG, carbohydrateG);
        this.proteinG = safeAdd(this.proteinG, proteinG);
        this.calciumMg = safeAdd(this.calciumMg, calciumMg);
        this.kaliumMg = safeAdd(this.kaliumMg, kaliumMg);
        this.ironMg = safeAdd(this.ironMg, ironMg);
        this.magnesiumMg = safeAdd(this.magnesiumMg, magnesiumMg);
        this.zincMg = safeAdd(this.zincMg, zincMg);
        this.celluloseG = safeAdd(this.celluloseG, celluloseG);
        this.aminoacidMg = safeAdd(this.aminoacidMg, aminoacidMg);
        this.leucineMg = safeAdd(this.leucineMg, leucineMg);
        this.methionineMg = safeAdd(this.methionineMg, methionineMg);
        this.seleniumUg = safeAdd(this.seleniumUg, seleniumUg);
        this.omega3G = safeAdd(this.omega3G, omega3G);
        this.vitaminAUg = safeAdd(this.vitaminAUg, vitaminAUg);
        this.vitaminBMg = safeAdd(this.vitaminBMg, vitaminBMg);
        this.folicacidUg = safeAdd(this.folicacidUg, folicacidUg);
        this.vitaminB12Ug = safeAdd(this.vitaminB12Ug, vitaminB12Ug);
        this.vitaminCMg = safeAdd(this.vitaminCMg, vitaminCMg);
        this.vitaminDUg = safeAdd(this.vitaminDUg, vitaminDUg);
        this.vitaminEMg = safeAdd(this.vitaminEMg, vitaminEMg);
    }

    /**
     * 모든 영양소 섭취량 삭제 (0 미만으로 내려가지 않음)
     */
    public void deleteMealIntake(double energyKcal, double carbohydrateG, double proteinG,
                                 double calciumMg, double kaliumMg, double ironMg, double magnesiumMg, double zincMg,
                                 double celluloseG, double aminoacidMg, double leucineMg, double methionineMg,
                                 double seleniumUg, double omega3G, double vitaminAUg, double vitaminBMg,
                                 double folicacidUg, double vitaminB12Ug, double vitaminCMg, double vitaminDUg, double vitaminEMg) {
        this.energyKcal = safeSub(this.energyKcal, energyKcal);
        this.carbohydrateG = safeSub(this.carbohydrateG, carbohydrateG);
        this.proteinG = safeSub(this.proteinG, proteinG);
        this.calciumMg = safeSub(this.calciumMg, calciumMg);
        this.kaliumMg = safeSub(this.kaliumMg, kaliumMg);
        this.ironMg = safeSub(this.ironMg, ironMg);
        this.magnesiumMg = safeSub(this.magnesiumMg, magnesiumMg);
        this.zincMg = safeSub(this.zincMg, zincMg);
        this.celluloseG = safeSub(this.celluloseG, celluloseG);
        this.aminoacidMg = safeSub(this.aminoacidMg, aminoacidMg);
        this.leucineMg = safeSub(this.leucineMg, leucineMg);
        this.methionineMg = safeSub(this.methionineMg, methionineMg);
        this.seleniumUg = safeSub(this.seleniumUg, seleniumUg);
        this.omega3G = safeSub(this.omega3G, omega3G);
        this.vitaminAUg = safeSub(this.vitaminAUg, vitaminAUg);
        this.vitaminBMg = safeSub(this.vitaminBMg, vitaminBMg);
        this.folicacidUg = safeSub(this.folicacidUg, folicacidUg);
        this.vitaminB12Ug = safeSub(this.vitaminB12Ug, vitaminB12Ug);
        this.vitaminCMg = safeSub(this.vitaminCMg, vitaminCMg);
        this.vitaminDUg = safeSub(this.vitaminDUg, vitaminDUg);
        this.vitaminEMg = safeSub(this.vitaminEMg, vitaminEMg);
    }

    // ==========================================
    //  Null Safety Helper Methods (내부 사용용)
    // ==========================================
    private Double safeAdd(Double origin, double valueToAdd) {
        return (origin == null ? 0.0 : origin) + valueToAdd;
    }
    private Double safeSub(Double origin, double valueToSub) {
        double result = (origin == null ? 0.0 : origin) - valueToSub;
        return Math.max(0.0, result); // 음수가 되지 않도록 방어
    }
}
