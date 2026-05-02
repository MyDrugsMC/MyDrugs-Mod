package org.mydrugs.mydrugs.core.drug;

public enum DrugCategory {
    OPIOID,         // heroin, morphine, fentanyl, oxycodone
    STIMULANT,      // cocaine, amphetamine, methamphetamine
    CANNABINOID,    // cannabis / weed, hashish, THC-based products
    PSYCHEDELIC,    // psilocybin mushrooms, LSD, mescaline, DMT
    DISSOCIATIVE,   // ketamine, PCP, DXM
    DEPRESSANT,     // alcohol, benzodiazepines, barbiturates
    EMPATHOGEN,     // MDMA
    DELIRIANT,      // datura, diphenhydramine at high doses
    INHALANT,       // nitrous oxide, solvents
    NICOTINIC,      // nicotine, tobacco
    CAFFEINE,       // caffeine
    NOOTROPIC,      // cognitive enhancers if you want modded/fantasy support
    RESEARCH_CHEMICAL,
    SEDATIVE,
    MIXED,          // drugs that don't fit neatly in one class
    OTHER;

    public int networkId() {
        return switch (this) {
            case OPIOID -> 0;
            case STIMULANT -> 1;
            case CANNABINOID -> 2;
            case PSYCHEDELIC -> 3;
            case DISSOCIATIVE -> 4;
            case DEPRESSANT -> 5;
            case EMPATHOGEN -> 6;
            case DELIRIANT -> 7;
            case INHALANT -> 8;
            case NICOTINIC -> 9;
            case CAFFEINE -> 10;
            case NOOTROPIC -> 11;
            case RESEARCH_CHEMICAL -> 12;
            case SEDATIVE -> 13;
            case MIXED -> 14;
            case OTHER -> 15;
        };
    }
}
