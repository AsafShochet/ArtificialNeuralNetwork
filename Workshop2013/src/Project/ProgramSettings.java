package Project;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class ProgramSettings
{

    // ---------------- Set source files location -----------------
    
    public static String timestamp = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").format(Calendar.getInstance().getTime());

    //path to the reac, spec and experiments files
    public static String basePath = "c:\\biologicalNetwork\\tests\\ann\\real_data\\";
    //public static String basePath = "c:\\biologicalNetwork\\tests\\TestOrGate\\";
    //public static String basePath = "c:\\biologicalNetwork\\tests\\TestAndGate\\";
    //public static String basePath = "c:\\biologicalNetwork\\tests\\TestNetwork1\\";
    
    public static String reactionsFileName = "egfr_reac_ambig.txt"; //"egfr_reac"
    public static String specificationsFileName = "egfr_spec.txt";
    public static String experimentsFileName = "egfr_exp.txt";

    // public static String weightsFile = graphs\\baseGraph_20130708_142534_bestweights_after.txt";
    // public static String weightsFile = "graphs\\20130716_071017\\baseGraph_20130716_071017_bestweights_after0.txt";
    
    public static String resultsFileName = "ResultsAnalysis.txt";
    public static String additionalResultsfilename = "baseGraph_" + timestamp;
    public static String resultsFolder = basePath + "results";
    public static String currentRunResultsfolder = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    
    public static boolean writeGraphFile = false;
    public static int booleanCalculations = 0;
    public static int neuralCalculations = 0;
    public static int booleansigmoidalCalculationsOne = 0;
    public static int booleansigmoidalCalculationsZero = 0;
    
    public static double GetRandomNumberFromList(double[] randomValues)
    {
	Random random = new Random();
	int randPosition = random.nextInt(randomValues.length);
	return randomValues[randPosition];
    }
    
    public static void SetBasePath(String path)
    {
	basePath = path;
	resultsFolder = basePath + "results";
    }
   
}
