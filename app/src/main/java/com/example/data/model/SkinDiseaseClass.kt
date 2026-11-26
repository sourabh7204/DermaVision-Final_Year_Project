package com.example.data.model

/**
 * 10 Skin Condition Classes for DermaVision V2
 * Based on the Skin Diseases Image Dataset (27,153 images).
 * MobileNetV3Small 224x224 RGB classifier.
 */
enum class SkinDiseaseClass(
    val classIndex: Int,
    val displayName: String,
    val shortName: String,
    val category: DiseaseCategory,
    val riskLevel: RiskLevel,
    val shortDescription: String,
    val detailedDescription: String,
    val commonSymptoms: List<String>,
    val educationalAdvice: String,
    val whenToSeeDoctor: String,
    val imageUrl: String
) {
    ECZEMA(
        classIndex = 0,
        displayName = "Eczema",
        shortName = "Eczema",
        category = DiseaseCategory.INFLAMMATORY,
        riskLevel = RiskLevel.LOW_TO_MODERATE,
        shortDescription = "Inflammatory skin condition characterized by dry, itchy, red, and irritated patches.",
        detailedDescription = "Eczema (dermatitis) is a common inflammatory skin disorder causing patches of skin to become inflamed, itchy, cracked, and rough. It can be triggered by allergens, environmental factors, stress, or dry climates.",
        commonSymptoms = listOf("Intense itching", "Dry, cracked, or scaly patches", "Red to brownish-gray discoloration", "Small raised bumps that may leak fluid"),
        educationalAdvice = "Keep skin moisturized with fragrance-free emollients, avoid harsh soaps, take lukewarm showers, and avoid known contact allergens.",
        whenToSeeDoctor = "Consult a dermatologist if patches become infected (oozing, yellow crusting), cause severe sleep disturbance, or do not respond to over-the-counter moisturizers.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/Eczema-arms.jpg/640px-Eczema-arms.jpg"
    ),

    WARTS_VIRAL(
        classIndex = 1,
        displayName = "Warts, Molluscum and other Viral Infections",
        shortName = "Viral Infections",
        category = DiseaseCategory.VIRAL,
        riskLevel = RiskLevel.LOW,
        shortDescription = "Contagious viral epidermal growths such as common warts, plantar warts, or molluscum contagiosum.",
        detailedDescription = "Cutaneous viral infections include warts (caused by human papillomaviruses - HPV) and molluscum contagiosum (caused by poxviruses). They manifest as firm, raised, or umbilicated bumps on various body surfaces.",
        commonSymptoms = listOf("Firm, fleshy, grainy bumps", "Rough surface with tiny black dots (clotted blood vessels)", "Small pearly papules with central dimple (molluscum)"),
        educationalAdvice = "Avoid picking or scratching lesions to prevent spreading. Do not share towels or razors, and keep affected areas covered if engaging in contact sports.",
        whenToSeeDoctor = "Consult a healthcare provider if lesions are painful, spreading rapidly, occurring on sensitive areas (face, genitals), or showing signs of secondary bacterial infection.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Verruca_vulgaris.JPG/640px-Verruca_vulgaris.JPG"
    ),

    MELANOMA(
        classIndex = 2,
        displayName = "Melanoma",
        shortName = "Melanoma",
        category = DiseaseCategory.MALIGNANT,
        riskLevel = RiskLevel.HIGH,
        shortDescription = "A serious form of skin cancer originating in pigment-producing melanocytes.",
        detailedDescription = "Melanoma is an aggressive form of skin cancer that develops in the melanocytes. It often resembles an unusual mole and can spread to other organs if not detected and treated early.",
        commonSymptoms = listOf("Asymmetrical mole shape", "Irregular, scalloped or notched borders", "Varied color (shades of brown, black, blue, white, or red)", "Diameter larger than 6mm (pencil eraser)", "Evolving size, shape, color, or elevation (ABCDE criteria)"),
        educationalAdvice = "Regular total-body skin self-exams using the ABCDE criteria are essential. Protect skin from UV exposure with SPF 30+ broad-spectrum sunscreen and protective clothing.",
        whenToSeeDoctor = "URGENT: Promptly consult a dermatologist or oncologist for any changing, bleeding, itching, or newly emerging pigmented lesion.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/Melanoma.jpg/640px-Melanoma.jpg"
    ),

    ATOPIC_DERMATITIS(
        classIndex = 3,
        displayName = "Atopic Dermatitis",
        shortName = "Atopic Dermatitis",
        category = DiseaseCategory.INFLAMMATORY,
        riskLevel = RiskLevel.LOW_TO_MODERATE,
        shortDescription = "A chronic, relapsing form of eczema often linked with allergic conditions and immune dysregulation.",
        detailedDescription = "Atopic Dermatitis is a chronic inflammatory disorder associated with a compromised skin barrier function and an overactive immune response. It frequently presents in skin folds (antecubital and popliteal fossae).",
        commonSymptoms = listOf("Severe persistent pruritus (itching)", "Erythematous plaques with excoriation", "Lichenification (skin thickening from rubbing)", "Dry, hypersensitive skin barrier"),
        educationalAdvice = "Implement a strict daily gentle skincare regimen. Apply heavy barrier ointments/creams within 3 minutes of bathing, maintain ambient humidity, and identify environmental flare triggers.",
        whenToSeeDoctor = "Seek medical evaluation if skin is weeping, crusting, or causing debilitating discomfort, as prescription topical anti-inflammatory therapies may be indicated.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Atopic_dermatitis_infant.jpg/640px-Atopic_dermatitis_infant.jpg"
    ),

    BASAL_CELL_CARCINOMA(
        classIndex = 4,
        displayName = "Basal Cell Carcinoma (BCC)",
        shortName = "Basal Cell Carcinoma",
        category = DiseaseCategory.MALIGNANT,
        riskLevel = RiskLevel.HIGH,
        shortDescription = "The most common form of skin cancer, arising in basal cells usually due to chronic sun exposure.",
        detailedDescription = "Basal Cell Carcinoma is a slow-growing skin cancer that develops in sun-exposed areas like the face, scalp, and neck. While it rarely metastasizes, it can cause significant local tissue damage if untreated.",
        commonSymptoms = listOf("Pearly, translucent, or waxy pink bump", "Lesion with visible tiny blood vessels (telangiectasias)", "Sore that bleeds, oozes, or crusts and repeatedly heals/reopens", "Scar-like white or yellowish area with poorly defined border"),
        educationalAdvice = "Strict sun protection is critical. Wear wide-brimmed hats and sunglasses, apply broad-spectrum sunscreen daily, and avoid tanning beds.",
        whenToSeeDoctor = "Schedule a clinical examination and biopsy with a dermatologist for non-healing sores or growing pearly bumps.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Basal_cell_carcinoma.JPG/640px-Basal_cell_carcinoma.JPG"
    ),

    MELANOCYTIC_NEVI(
        classIndex = 5,
        displayName = "Melanocytic Nevi (NV)",
        shortName = "Melanocytic Nevus (Mole)",
        category = DiseaseCategory.BENIGN,
        riskLevel = RiskLevel.LOW,
        shortDescription = "Common benign moles composed of clusters of melanocytes with uniform color and distinct borders.",
        detailedDescription = "Melanocytic Nevi are benign pigmented skin lesions commonly known as moles. They arise from proliferation of melanocytes and are typically round, symmetrical, and uniformly pigmented (light brown, dark brown, or flesh-colored).",
        commonSymptoms = listOf("Uniform brown, tan, or flesh color", "Distinct, smooth, well-defined circular border", "Symmetrical shape", "Stable size over long periods"),
        educationalAdvice = "Monitor moles periodically for any ABCDE changes (Asymmetry, Border irregularity, Color changes, Diameter growth, Evolution).",
        whenToSeeDoctor = "Consult a dermatologist if a nevus undergoes rapid change in size, shape, border, or begins to bleed, itch, or ulcerate.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Melanocytic_nevus.jpg/640px-Melanocytic_nevus.jpg"
    ),

    BENIGN_KERATOSIS(
        classIndex = 6,
        displayName = "Benign Keratosis-like Lesions (BKL)",
        shortName = "Benign Keratosis (BKL)",
        category = DiseaseCategory.BENIGN,
        riskLevel = RiskLevel.LOW,
        shortDescription = "Non-cancerous epidermal lesions including solar lentigines and lichenoid keratoses.",
        detailedDescription = "Benign Keratosis-like Lesions represent a group of benign skin growths such as solar lentigines (age/sun spots), seborrheic keratosis variants, and lichen planus-like keratoses resulting from sun exposure and aging.",
        commonSymptoms = listOf("Flat or slightly elevated brown/tan spots", "Rough or slightly verrucous texture", "Clearly demarcated borders", "Non-tender and non-infiltrative"),
        educationalAdvice = "These lesions are benign. Use daily UV protection to reduce development of new photo-aging spots.",
        whenToSeeDoctor = "Dermatological evaluation is recommended if a lesion exhibits atypical pigment variegation or borders to distinguish it from lentigo maligna.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Seborrheic_keratosis_4.jpg/640px-Seborrheic_keratosis_4.jpg"
    ),

    PSORIASIS_LICHEN_PLANUS(
        classIndex = 7,
        displayName = "Psoriasis, Lichen Planus and related diseases",
        shortName = "Psoriasis & Lichen Planus",
        category = DiseaseCategory.INFLAMMATORY,
        riskLevel = RiskLevel.LOW_TO_MODERATE,
        shortDescription = "Autoimmune & inflammatory disorders producing silvery scales or violaceous planar papules.",
        detailedDescription = "This group encompasses systemic inflammatory dermatoses: Psoriasis (accelerated skin cell turnover forming thick plaques with silvery scales) and Lichen Planus (inflammatory reaction presenting with the '6 Ps': purple, polygonal, pruritic, planar papules/plaques).",
        commonSymptoms = listOf("Thick red plaques covered with silvery scales (psoriasis)", "Purple, polygonal, itchy, flat-topped bumps (lichen planus)", "Involvement of elbows, knees, scalp, or lower back", "Nail pitting or nail dystrophy"),
        educationalAdvice = "Maintain adequate skin hydration, avoid mechanical trauma (Koebner phenomenon), manage stress, and avoid known trigger medications.",
        whenToSeeDoctor = "Consult a physician for a tailored management plan including prescription topical treatments, phototherapy, or systemic medications.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/78/Psoriasis_on_back.jpg/640px-Psoriasis_on_back.jpg"
    ),

    SEBORRHEIC_KERATOSES(
        classIndex = 8,
        displayName = "Seborrheic Keratoses and other Benign Tumors",
        shortName = "Seborrheic Keratoses",
        category = DiseaseCategory.BENIGN,
        riskLevel = RiskLevel.LOW,
        shortDescription = "Common non-cancerous 'stuck-on' waxy or verrucous growths occurring in mature skin.",
        detailedDescription = "Seborrheic Keratoses are extremely common benign epidermal growths. They often appear as if dabbed onto the skin with candle wax, varying from light tan to dark black with a pasted-on appearance.",
        commonSymptoms = listOf("Waxy, scaly, or 'stuck-on' appearance", "Round or oval shape", "Color ranging from light tan to deep black", "Greasy or rough surface texture"),
        educationalAdvice = "These growths are completely harmless and non-contagious. Avoid picking or scratching them to prevent irritation.",
        whenToSeeDoctor = "Evaluation is advised if a growth bleeds from friction, becomes inflamed, or if its appearance causes diagnostic uncertainty.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Seborrheic_keratosis.JPG/640px-Seborrheic_keratosis.JPG"
    ),

    FUNGAL_INFECTIONS(
        classIndex = 9,
        displayName = "Tinea, Ringworm, Candidiasis and other Fungal Infections",
        shortName = "Fungal Infections",
        category = DiseaseCategory.FUNGAL,
        riskLevel = RiskLevel.LOW_TO_MODERATE,
        shortDescription = "Superficial cutaneous fungal and yeast infections such as ringworm, athlete's foot, or thrush.",
        detailedDescription = "Superficial mycoses caused by dermatophytes (Tinea corporis, cruris, pedis, capitis) or yeasts (Candida, Malassezia). They affect keratinized tissue and warm, moist skin folds.",
        commonSymptoms = listOf("Circular ring-shaped rash with raised, active borders", "Central clearing of lesion (ringworm pattern)", "Scaling, maceration, and intense burning/itching", "Bright red rash in skin folds with satellite lesions (candida)"),
        educationalAdvice = "Keep skin clean and dry, change out of sweaty clothes promptly, wear breathable fabrics, and do not share personal grooming items.",
        whenToSeeDoctor = "Consult a doctor if over-the-counter topical antifungal creams fail to resolve the infection within 2 weeks or if widespread.",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Tinea_corporis.jpg/640px-Tinea_corporis.jpg"
    );

    companion object {
        fun fromIndex(index: Int): SkinDiseaseClass? {
            return entries.find { it.classIndex == index }
        }

        fun getAllClasses(): List<SkinDiseaseClass> = entries.toList()
    }
}

enum class DiseaseCategory(val label: String) {
    INFLAMMATORY("Inflammatory"),
    VIRAL("Viral Infection"),
    MALIGNANT("Malignant / Neoplastic"),
    BENIGN("Benign Lesion"),
    FUNGAL("Fungal Infection")
}

enum class RiskLevel(val label: String) {
    LOW("Low Risk / Benign"),
    LOW_TO_MODERATE("Low to Moderate"),
    HIGH("High / Requires Clinical Attention")
}
