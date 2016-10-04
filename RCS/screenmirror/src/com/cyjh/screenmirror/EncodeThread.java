package com.cyjh.screenmirror;

public class EncodeThread extends Thread {

	private EncodeDevice m_venc;
	
	public EncodeThread(EncodeDevice enc) {
		m_venc = enc;
	}
	
	public void run(){
		while(true) {
			m_venc.stream();
		}
	}
	
}
