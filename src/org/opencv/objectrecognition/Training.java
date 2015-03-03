public class Training {

public static void main(String[] args) {

    String trainingDatapath="\\train";
    String evalDatapath="\\evam";
    BOW bow = null;

    try {
                 bow = new BOW();
    } 

    catch (Exception e) {

        e.getStackTrace();
    }

    bow.extractTrainingVocabulary(trainingDatapath);


    CvMat dictionary  = bow.getbMeansTrainer().cluster();

    bow.getbDescriptorExtractor().setVocabulary(dictionnary);


    CvMat evalData = new CvMat(bow.dictionarySize);

    bow.extractBOWDescriptor(evalDatapath, evalData);


}
}