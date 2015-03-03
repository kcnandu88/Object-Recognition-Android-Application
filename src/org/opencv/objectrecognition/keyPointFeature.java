
package org.opencv.objectrecognition;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.opencv.objectrecognition.*;
import org.opencv.core.*;
import org.opencv.features2d.KeyPoint;
import org.opencv.android.*;

public class keyPointFeature
{
	public boolean FromFile(FileReader fp) throws IOException
	{
		char[] temp=new char[1024];
		int rows, cols, type;
		fp.read (temp, 4, 1); rows=Integer.parseInt(new String(temp));
		fp.read (temp, 4, 1); cols=Integer.parseInt(new String(temp));
		fp.read (temp, 4, 1); type=Integer.parseInt(new String(temp));
		Mat m=new Mat(rows, cols, type);

		int cn=m.depth();
		fp.read (temp, rows*cols*cn, 1);
		descriptors=m;

		keypoints.clear();
		int nfeatureNum;
		fp.read (temp, 4, 1); nfeatureNum=Integer.parseInt(new String(temp));
		for (int i=0; i<nfeatureNum; i++)
		{
			KeyPoint point=new KeyPoint();
			fp.read (temp, 4, 1); point.pt.x=Integer.parseInt(new String(temp));
			fp.read (temp, 4, 1); point.pt.y=Integer.parseInt(new String(temp));
			fp.read (temp, 4, 1); point.size=Integer.parseInt(new String(temp));
			fp.read (temp, 4, 1); point.angle=Float.parseFloat(new String(temp));
			fp.read (temp, 4, 1); point.response=Float.parseFloat(new String(temp));
			fp.read (temp, 4, 1); point.octave=Integer.parseInt(new String(temp));
			fp.read (temp, 4, 1); point.class_id=Integer.parseInt(new String(temp));
			keypoints.addLast(point);
		}
		return true;
	}
	Mat descriptors;
	LinkedList<KeyPoint> keypoints;
}