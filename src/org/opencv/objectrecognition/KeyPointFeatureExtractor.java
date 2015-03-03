
package org.opencv.objectrecognition;
import java.util.LinkedList;
import java.util.List;

import org.opencv.objectrecognition.*;
import org.opencv.utils.Converters;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.*;




public class KeyPointFeatureExtractor
{
	private final int SHEAR_TH = 5;
	private final int SCALE_RATIO_TH = 3;
	private final int SCALE_TH = 5;
	private final double DBL_EPSILON = 0.00001;

	public static FeatureDetector		featureDetector = FeatureDetector.create(4); // SURF
	public static DescriptorExtractor	descExtractor = DescriptorExtractor.create (4); // SURF
	public static DescriptorMatcher		descMatcher = DescriptorMatcher.create(1); // FLANN based

	public Boolean extract(Mat img, keyPointFeature keypointfeature)
	{
		Mat gray = new Mat (img.height(), img.width(), CvType.CV_8UC1);
		Imgproc.cvtColor (img, gray, Imgproc.COLOR_RGB2GRAY);
		MatOfKeyPoint matkeypoint=new MatOfKeyPoint();
		featureDetector.detect(gray, matkeypoint);
		Converters.Mat_to_vector_KeyPoint(matkeypoint, keypointfeature.keypoints);
		MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
		MatOfDMatch matches = new MatOfDMatch();
		descExtractor.compute(img, keypoints_object, matches);
		if (keypointfeature.keypoints.size() == 0)
			return false;
		return true;
	}
	public double match(keyPointFeature feature1, keyPointFeature feature2)
	{
		MatOfDMatch matches = new MatOfDMatch();

		descMatcher.match(feature1.descriptors, feature2.descriptors, matches);
		List<DMatch> matchesList = matches.toList();

		double maxd = 0; double mind = 10000.0;
		for( int k = 0; k < feature1.descriptors.height(); k++ )
		{ 
			double dist = matchesList.get(k).distance;
			if( dist < mind ) mind = dist;
		}
		LinkedList<DMatch> good_matches = new LinkedList<DMatch>();

		for( int i = 0; i < feature1.descriptors.rows(); i++ )
		{
			if( matchesList.get(i).distance < 3*mind)
			{
				good_matches.addLast( matchesList.get(i)); 
			}
		}

		//-- Localize the object from img_1 in img_2
		LinkedList<Point> objList = new LinkedList<Point>();
		LinkedList<Point> sceneList = new LinkedList<Point>();


		for( int i = 0; i < good_matches.size(); i++ )
		{
			objList.addLast( feature1.keypoints.get(good_matches.get(i).queryIdx ).pt );
			sceneList.addLast( feature2.keypoints.get( good_matches.get(i).trainIdx ).pt );
		}

		if (good_matches.size()<4)
			return mind;
//		return mind;
		MatOfPoint2f obj = new MatOfPoint2f();
		obj.fromList(objList);

		MatOfPoint2f scene = new MatOfPoint2f();
		scene.fromList(sceneList);
		
		Mat H = Calib3d.findHomography( obj, scene );

		//-- Get the corners from the image_1 ( the object to be "detected" )
		if (VerificationByAffineTransform(H))
		{
			Mat scenecorrednMat=new Mat();
			LinkedList<Point> scenecorrected=new LinkedList<Point>();
			Core.perspectiveTransform( obj, scenecorrednMat, H);
			Converters.Mat_to_vector_Point2f(scenecorrednMat, scenecorrected);
			float distsum=0.0f;
			for (int i=0; i<scenecorrected.size(); i++)
			{
				Point pt, pt2;
				double dist=Math.sqrt((sceneList.get(i).x-scenecorrected.get(i).x)*(sceneList.get(i).x-scenecorrected.get(i).x)+
					(sceneList.get(i).y-scenecorrected.get(i).y)*(sceneList.get(i).y-scenecorrected.get(i).y));
				distsum += dist;
			}
			distsum /= scenecorrected.size();
			return mind;
		}
		else
		{
			return 1000.0f;
		}
	}
	boolean VerificationByAffineTransform(Mat _pA)
	{
		double[] rS1=new double[1];
		double[] rS2=new double[1];
		double[] rAlpha=new double[1];
		double[] rShear=new double[1];
		double rRatScale;
	
		if (!GetAffineTransformProperty(_pA, rS1, rS2, rAlpha, rShear))
			return false;
	
		if (Math.abs(rShear[0]) > SHEAR_TH)
			return false;
		
		rRatScale = Math.abs(rS1[0] / rS2[0]);
		if (rRatScale < 1)
			rRatScale = 1 / rRatScale;
	
		if (rRatScale > SCALE_RATIO_TH)
			return false;
	
		rS1[0] = Math.abs(rS1[0]);
		if (rS1[0] < 1)
			rS1[0] = 1 / rS1[0];
	
		if (rS1[0] > SCALE_TH)
			return false;
	
		rS2[0] = Math.abs(rS2[0]);
		if (rS2[0] < 1)
			rS2[0] = 1 / rS2[0];
			
		if (rS2[0] > SCALE_TH)
			return false;
	
		return true;
	}
	
	boolean GetAffineTransformProperty(Mat _pA, double[] _rSx, double[] _rSy, double[] _rAlp, double[] _rShear)
	{
		double rCos, rSin;
		double[][] pprA=new double[3][3];
		for (int i=0; i<3; i++)
		{
			for(int j=0; j<3;j++)
				pprA[i][j]=_pA.get(i, j)[0];
		}
		
		if ( Math.abs(pprA[0][0]) < DBL_EPSILON && Math.abs(pprA[0][1]) < DBL_EPSILON )
			return false;
		if ( Math.abs(pprA[1][0]) < DBL_EPSILON && Math.abs(pprA[1][1]) < DBL_EPSILON)
			return false;
		
		_rAlp[0] = Math.atan2(pprA[1][0], pprA[1][1]);
		_rSy[0] = Math.sqrt(pprA[1][0] * pprA[1][0] + pprA[1][1] * pprA[1][1]);
		if (Math.abs(_rSy[0]) < DBL_EPSILON)
			return false;
		rCos = Math.cos(_rAlp[0]);	
		rSin = Math.sin (_rAlp[0]);
		
		_rShear[0] = (pprA[0][0] * rSin + pprA[0][1] * rCos) / _rSy[0];
		
		_rSx[0] = pprA[0][0] * rCos - pprA[0][1] * rSin;
		if (Math.abs(_rSx[0]) < DBL_EPSILON)
			return false;
		return true;
	}

}


