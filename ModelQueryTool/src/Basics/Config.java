package Basics;

public class Config {
    private static boolean WORK_ON_CLEANED = true;

    private static boolean STANDARDIZED = true;

    private static int K_BEST = 5;

    private static String DISTANCE_FUNCTION = "_cosine";

    private static String MATCHING_METHOD = "brute_force";

    private static String PYTHON_HANDLE = "py";

    // <editor-fold desc="getters">
    public static boolean workOnCleaned() {
        return WORK_ON_CLEANED;
    }

    public static boolean standardized() {
        return STANDARDIZED;
    }

    public static int kBest() {
        return K_BEST;
    }

    public static String getDistanceFunction() {
        return DISTANCE_FUNCTION;
    }

    public static String getMatchingMethod() {
        return MATCHING_METHOD;
    }

    public static String getPythonHandle() {
        return PYTHON_HANDLE;
    }
    // </editor-fold>

    // <editor-fold desc="setters">
    static void setWorkOnCleaned(boolean workOnCleaned) {
        WORK_ON_CLEANED = workOnCleaned;
        String type = "original";
        if (WORK_ON_CLEANED) type = "cleaned";
        System.out.println("Targeting " + type + " files for preprocessing steps.");
    }

    static void setStandardized(boolean STANDARDIZED) {
        Config.STANDARDIZED = STANDARDIZED;
        String type = "min-max normalization";
        if (STANDARDIZED) type = "standardization";
        System.out.println("Using " + type + " as the normalization method.");
    }

    static void setkBest(int kBest) {
        K_BEST = kBest;
        System.out.println("Retrieving " + K_BEST + " query results.");
    }

    static void setDistanceFunction(String distanceFunction) {
        DISTANCE_FUNCTION = distanceFunction;
        System.out.println("Using the " + DISTANCE_FUNCTION + " distance function.");
    }

    static void setMatchingMethod(String matchingMethod) {
        MATCHING_METHOD = matchingMethod;
        System.out.println("Using the " + MATCHING_METHOD + " matching method.");
    }

    static void setPythonHandle(String pythonHandle) {
        PYTHON_HANDLE = pythonHandle;
        System.out.println("Using the " + PYTHON_HANDLE + " python command.");
    }
    // </editor-fold>
}
