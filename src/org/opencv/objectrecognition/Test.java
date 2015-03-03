package separate;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_ml.CvSVMParams;
import com.googlecode.javacv.cpp.opencv_features2d.*;
import com.googlecode.javacv.cpp.opencv_nonfree.SURF;
import com.googlecode.javacv.cpp.opencv_ml.CvSVM;;




public class TrainImage {
	
	public void trainSVM(Map<String, ArrayList<CvMat>> numberClasses, int responseCols, int responseType){
		

		int sampleSize = 0;
		for(Map.Entry<String, ArrayList<CvMat>> entry : numberClasses.entrySet()){
			for (CvMat m : entry.getValue()){
				sampleSize += m.rows();
			}
		}
		
		
		for(Map.Entry<String, ArrayList<CvMat>> entry : numberClasses.entrySet()){
			String className = entry.getKey();
			
			
			CvMat samples = CvMat.create(sampleSize, responseCols, responseType);
			CvMat labels = CvMat.create(sampleSize, 1, opencv_core.CV_32FC1);
			
			
			int targetSampleSize = 0;			
			
			for (CvMat m : entry.getValue()){
				samples.put(m);
				targetSampleSize += m.rows();
			}
			
			
			System.out.println("targetSampleSize = " + targetSampleSize);
			int nonTargetSize = sampleSize - targetSampleSize;
			System.out.println("nonTargetSize = " + nonTargetSize);
			
			for (int i = 0; i < targetSampleSize; i++){
				labels.put(0, i, 1.0);
			}
			
			int start = targetSampleSize;
			for(Map.Entry<String, ArrayList<CvMat>> entry1 : numberClasses.entrySet()){
				String newClassName = entry1.getKey();
				if(newClassName == className)continue;
				for (CvMat m : entry.getValue()){
					samples.put(m);
				}
				
				int step = entry1.getValue().size();
				for(int j = start; j < (start + step); j++){
					labels.put(0, j, 0.0);
				}
				
				start += entry1.getValue().size();
				
			}
				
			CvMat samples_32f = CvMat.create(samples.rows(), samples.cols(), opencv_core.CV_32F);
			opencv_core.cvConvert(samples, samples_32f);
			
			if(samples.rows() ==0)continue;
			CvSVM  classifier = new CvSVM();
			
			CvSVMParams params = new CvSVMParams();
			params.svm_type(100);
			params.kernel_type(2);
			params.gamma(0.5);
			CvTermCriteria criteria = new CvTermCriteria(opencv_core.CV_TERMCRIT_ITER, 1000, 1e-6);
			params.term_crit(criteria);
			
			classifier.train(samples_32f, labels, new CvMat(null), new CvMat(null), params);
			classifier.get_support_vector_count();
			
			String fileName = extractor + ".txt";
			classifier.save(fileName,null);
		
			
		}			
	}
	
	public void extractSamples(FeatureDetector detector, BOWImgDescriptorExtractor bowide, Map<String, ArrayList<CvMat>> numberClasses){
		
		System.out.println("extract samples...");
		
		String fileInput = "training.txt";
		File file = new File(fileInput);
		int totalSamples = 0;
		Vector<String> lines = new Vector<String>();
		
		try{
			
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null){
				lines.add(s);
				
			}
			
		}catch(Exception e){}
		
		ArrayList<CvMat> responseHistSet;
		
		for (int i = 0; i < lines.size(); i++){
			KeyPoint keyPoints = new KeyPoint();
			CvMat responseHist = new CvMat(null);
			CvMat descriptors = new CvMat(null);
			
			IplImage image;
			
			String line[] = lines.get(i).split(" ");
			String filepath = line[0];
			String key = "class_" + line[1];
			
			image = cvLoadImage(filepath);
			
			detector.detect(image,keyPoints, null);
			bowide.compute(image, keyPoints, responseHist, null, descriptors);
			
			if (!numberClasses.containsKey(key)){
				responseHistSet = new ArrayList<CvMat>();
				numberClasses.put(key, responseHistSet);
			}
			numberClasses.get(key).add(responseHist);
			
			System.out.print(".");
			
			totalSamples++;
		}
		
		CvMat hist;	
		ArrayList<Integer> size = new ArrayList<Integer>();
		
		int i = 0;
		for(Map.Entry<String, ArrayList<CvMat>> entry : numberClasses.entrySet()){
			int rows = 0;
			for (CvMat m : entry.getValue()){
				rows += m.rows();
			}
			size.add(i, rows);
			i++;
		}
		
		
		CvFileStorage fs = CvFileStorage.open("training_samples.yml", null, 1);
		int j = 0;	
		for(Map.Entry<String, ArrayList<CvMat>> entry : numberClasses.entrySet()){
			
			
			int histSize = size.get(j);			
			hist = CvMat.create(histSize,bowide.descriptorSize(),bowide.descriptorType());
			
			int x = 0;
			
			for (CvMat m : entry.getValue()){
				
				int cols = m.cols();
				double[] test = m.get();
				
				if(x < histSize){
					for (int y = 0; y < cols; y++){
						
						double digit = test[y];
						hist.put(x, y, digit);
					}
				}
				x++;
			}
			
			opencv_core.cvWrite(fs,entry.getKey(), hist);
			hist.deallocate();
			
			j++;
		}
		opencv_core.cvReleaseFileStorage(fs);
	}
	
	
	public static void main(String[] args) {
		
		
		
		double hessianThreshold = 2500d;
		int nOctaves = 4;
		int nOctaveLayers = 2;
		boolean extended = true;
		boolean upright = false;
		SURF surf = new SURF(hessianThreshold, nOctaves, nOctaveLayers, extended, upright);		
		String path="*\\TrainingImages";
		 File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		int total=0;


    for (File file : listOfFiles) {
        if (file.isFile()) {
            total++;

        }
    }
    System.out.println(total);

    CvMat training_mat=CvMat.create(total, 400, CV_32FC3);
    CvMat labels=CvMat.create(total, 1,CV_32FC1);

    int filenum=0;
    for(File file:listOfFiles)
    {

        if (file.isFile()) {

            String imageName=file.getName();    
            String filePath=path+"/"+imageName;
            CvMat image=cvLoadImageM(filePath);

            CvMat smallerImage=CvMat.create(20, 20,CV_8UC3);    
            cvResize(image,smallerImage, CV_INTER_LINEAR);
    }

		
		Pointer pointer = opencv_core.cvReadByName(fs, null, "vocabulary", null);
		CvMat vocabulary = new CvMat(pointer);
		opencv_core.cvReleaseFileStorage(fs);
		
		FeatureDetector detector = FeatureDetector.create("SURF");
		DescriptorExtractor surfExtractor = DescriptorExtractor.create("SURF");
		DescriptorExtractor extractor = new OpponentColorDescriptorExtractor(surfExtractor);
		DescriptorMatcher matcher = DescriptorMatcher.create("BruteForce");
		
		BOWImgDescriptorExtractor bowide = new BOWImgDescriptorExtractor(extractor, matcher);
		bowide.setVocabulary(vocabulary);
				
		Map<String, ArrayList<CvMat>> numberClasses = new HashMap<String, ArrayList<CvMat>>();
		numberClasses.clear();
		
		TrainImage ti = new TrainImage();
		ti.extractSamples(detector, bowide, numberClasses);

		int number = numberClasses.keySet().size();
		
		for(Map.Entry<String, ArrayList<CvMat>> entry : numberClasses.entrySet()){
			String className = entry.getKey();
			int size = entry.getValue().size();
		}
		
		
		ti.trainSVM(numberClasses, bowide.descriptorSize(), bowide.descriptorType());
		
	}
}