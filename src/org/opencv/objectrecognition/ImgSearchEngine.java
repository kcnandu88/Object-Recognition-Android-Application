
package org.opencv.objectrecognition;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.opencv.objectrecognition.*;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.*;

public class ImgSearchEngine
{
	private KeyPointFeatureExtractor	m_keyFeaExtractor;
	private int		nlaskley;
	private LinkedList<Searchelem>		m_features;
	private LinkedList<String>			m_filenames;
	private int				m_ntotal;
	private static final int STD_MIN_SIZE = 60000;

	public boolean extractFeatureFromImgPath(String szimg, keyPointFeature feature)
	{
		Mat img; 
		img = Highgui.imread(szimg);
		if (img.empty()) return false;
	
		int nrate=(int)(Math.sqrt((img.height()*img.width())/STD_MIN_SIZE)+0.5f);
		if (nrate==0) nrate=1;
		Size newsize=new Size(img.width()/nrate,img.height()/nrate);
		Mat scaledimg=new Mat();
		Imgproc.resize(img, scaledimg, newsize);
		img=scaledimg;
		if (!m_keyFeaExtractor.extract(img, feature))
			return false;
		return true;
	}
	
	boolean recognize (String szimgname, int[] objectidx, double[] rdist)
	{
		keyPointFeature query=new keyPointFeature();
		if (!extractFeatureFromImgPath (szimgname, query))
			return false;
	
		int i;
		int nDBSize=m_features.size();
	
		int[] idxs=new int[nDBSize];
		double[] dists=new double[nDBSize];
		for (i=0; i < nDBSize; i++)
		{
			Searchelem db=m_features.get(i);
			if (db.feature.keypoints.size() == 0)
			{
				dists[i]=10000.0;
				idxs[i]=i;
				continue;
			}
			dists[i] = m_keyFeaExtractor.match (query, db.feature);
			idxs[i] = i;
		}
		int minIdx=-1;
		double min_dist=1000000.0;
		for (i=0; i < nDBSize; i++)
		{
			if (min_dist>dists[i])
			{
				min_dist=dists[i];
				minIdx = i;
			}
		}
	
		objectidx=new int[1];
		objectidx[0]= minIdx;
		rdist=new double[1];
		rdist[0]= min_dist;
		return true;
	}
	
	boolean loadDB(String szfilename) throws IOException
	{
		nlaskley=0;
		File fp=new File(szfilename);
		if (!fp.isFile()) return false;
		int total;
		FileReader fileReader=new FileReader(szfilename); 
		char[] temp=new char[1024];
		fileReader.read (temp, 4, 1); total=Integer.parseInt(new String(temp));

		for (int i=0; i<total; i++)
		{
			Searchelem elem=new Searchelem();
			fileReader.read (temp, 260, 1); total=Integer.parseInt(new String(temp));
			String s=temp.toString();
			elem.feature.FromFile(fileReader);
			elem.key = nlaskley++;
			m_filenames.addLast(s);
			m_features.addLast(elem);
		}

		return true;
	}
	
	int count()
	{
		return m_features.size();
	}
}


