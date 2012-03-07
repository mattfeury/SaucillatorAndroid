package com.mattfeury.saucillator.dev.android.visuals;

public class ComplexNum {
	
	private float real, imag;

	public ComplexNum(float real, float imag){
		this.real = real;
		this.imag = imag;
		
	}
	
	public ComplexNum(ComplexNum toClone){
		this(toClone.getReal(), toClone.getImag());
	}
	
	public ComplexNum(){
		this(1, 0);
	}
	
	
	ComplexNum multiply(ComplexNum rhs){
		return new ComplexNum(real * rhs.getReal() - imag * rhs.getImag(), imag * rhs.getReal() + real * rhs.getImag());
		
	}
	
	ComplexNum pow(int power){
		if (power == 0)
			return new ComplexNum(1, 0);
		
		ComplexNum ret = new ComplexNum(this);
		
		if (power == 1)
			return ret;
		
		for(int i = 1; i < power; i++){
			
			ret = ret.multiply(this);
		}
		
		return ret;
		
	}
	
	public float getReal(){
		return real;
	}
	
	public float getImag(){
		return imag;
	}
	
}
