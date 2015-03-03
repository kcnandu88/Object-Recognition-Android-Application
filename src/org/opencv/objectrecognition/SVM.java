public class SVM {

	
	public void do_svm(CvMat descriptors,CvMat labels){
	CvSVMParams params =new CvSVMParams();


    params.svm_type(CvSVM.C_SVC);
    params.kernel_type(CvSVM.POLY);
    params.term_crit(cvTermCriteria(CV_TERMCRIT_ITER, 100, 1e-6));
    params.gamma(3);
    params.degree(CvSVM.DEGREE);

    CvSVM svm =new CvSVM();
    svm.train(descriptors,labels,new CvMat(CV_32FC3),new CvMat(CV_32FC3),params);
    svm.save("","trainsvm.txt");
	
	}
	}