public class BOW {

  final String trainingData ="";
  final String testData="";

  int dictionarySize = 500;

  int retries = 1;

  int flags = CV_KMEANS_USE_INITIAL_LABELS;

  CvTermCriteria termcrit ;

  BOWKMeansTrainer bMeansTrainer ;

  BOWImgDescriptorExtractor bDescriptorExtractor;

  FeatureDetector featureDetector;

  DescriptorExtractor descriptorExtractor;

  DescriptorMatcher descriptorMatcher;

  SVM svm=new SVM();
public BOWKMeansTrainer getbMeansTrainer() {
    return bMeansTrainer;
}


public void setbMeansTrainer(BOWKMeansTrainer bMeansTrainer) {
    this.bMeansTrainer = bMeansTrainer;
}


public BOWImgDescriptorExtractor getbDescriptorExtractor() {
    return bDescriptorExtractor;
}


public void setbDescriptorExtractor(
        BOWImgDescriptorExtractor bDescriptorExtractor) {
    this.bDescriptorExtractor = bDescriptorExtractor;
}


public FeatureDetector getFeatureDetector() {
    return featureDetector;
}


public void setFeatureDetector(FeatureDetector featureDetector) {
    this.featureDetector = featureDetector;
}


public DescriptorExtractor getDescriptorExtractor() {
    return descriptorExtractor;
}


public void setDescriptorExtractor(DescriptorExtractor descriptorExtractor) {
    this.descriptorExtractor = descriptorExtractor;
}


public DescriptorMatcher getDescriptorMatcher() {
    return descriptorMatcher;
}


public void setDescriptorMatcher(DescriptorMatcher descriptorMatcher) {
    this.descriptorMatcher = descriptorMatcher;
}


 public void extractTrainingVocabulary (String  path ) 
  {
    File dir = new File(path);
	File[] listOfFiles = folder.listFiles();
	featureDetector = FeatureDetector.create("SURF");
    descriptorExtractor=DescriptorExtractor.create("SURF");
    descriptorMatcher= DescriptorMatcher.create("BFMatcher");
    termcrit = new CvTermCriteria(CV_TERMCRIT_ITER, 10, 0.001);
    bMeansTrainer = new BOWKMeansTrainer(dictionarySize, termcrit , retries, flags);
    bDescriptorExtractor = new BOWImgDescriptorExtractor( descriptorExtractor, descriptorMatcher);
	
	
    for (File file : listOfFiles)
    {      
            if (child.getName().contains(".jpg"))
            {
                 final CvMat image = cvLoadImageM(child.getName());
                 if (!image.empty())
                 {

                    KeyPoint keypoints = new KeyPoint();
                    featureDetector.detect(image, keypoints, null);       
                    {
                        CvMat features = new CvMat() ; // 
                        descriptorExtractor.compute(image, keypoints, features);
                        bMeansTrainer.add(features);
                    }
                 }
            }
        }
    }
  

public void extractBOWDescriptor (String path , CvMat descriptors ) 

  {
    File dir = new File(path);
	File[] listOfFiles = folder.listFiles();
	int total=0;
	featureDetector = FeatureDetector.create("SURF");
    descriptorExtractor=DescriptorExtractor.create("SURF");
    descriptorMatcher= DescriptorMatcher.create("BFMatcher");
    termcrit = new CvTermCriteria(CV_TERMCRIT_ITER, 10, 0.001);
    bMeansTrainer = new BOWKMeansTrainer(dictionarySize, termcrit , retries, flags);
    bDescriptorExtractor = new BOWImgDescriptorExtractor( descriptorExtractor, descriptorMatcher);
	
    for (File file : listOfFiles)
    {      
            if (file.getName().contains(".jpg"))
            {
                 final CvMat image = cvLoadImageM(file.getName());

                 if (!image.empty())
                 {

                    KeyPoint keypoints = new KeyPoint(); 
                    featureDetector.detect(image, keypoints, null);

                    if (keypoints.isNull())
                    {
                                file.getName()
                    }

                    else 
                    {
                        CvMat BoWdescriptors = new CvMat() ; 
                        bDescriptorExtractor.compute(image, keypoints, BoWdescriptors, null, null);

                        descriptors.put(descriptors);  
                         }
                 }  
            }
        }
		CvMat labels=CvMat.create(total, 1,CV_32FC1);
		svm.do_svm(labels,descriptors);
    }
  }
