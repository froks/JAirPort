/*
** AlacFile.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
package com.beatofthedrum.alacdecoder;

public class AlacFile {
  byte input_buffer[];
  int ibIdx = 0;
  int input_buffer_bitaccumulator = 0; /* used so we can do arbitary
						bit reads */

  int samplesize = 0;
  int numchannels = 0;
  int bytespersample = 0;

  LeadingZeros lz = new LeadingZeros();


  private int buffer_size = 16384;
  /* buffers */
  int predicterror_buffer_a[] = new int[buffer_size];
  int predicterror_buffer_b[] = new int[buffer_size];

  int outputsamples_buffer_a[] = new int[buffer_size];
  int outputsamples_buffer_b[] = new int[buffer_size];

  int uncompressed_bytes_buffer_a[] = new int[buffer_size];
  int uncompressed_bytes_buffer_b[] = new int[buffer_size];


  /* stuff from setinfo */
  int setinfo_max_samples_per_frame = 0; // 0x1000 = 4096
  /* max samples per frame? */

  int setinfo_sample_size = 0; // 0x10
  int setinfo_rice_historymult = 0; // 0x28
  int setinfo_rice_initialhistory = 0; // 0x0a
  int setinfo_rice_kmodifier = 0; // 0x0e
  /* end setinfo stuff */

  public void setSetinfo_sample_size(int setinfo_sample_size) {
    this.setinfo_sample_size = setinfo_sample_size;
  }

  public void setSetinfo_rice_historymult(int setinfo_rice_historymult) {
    this.setinfo_rice_historymult = setinfo_rice_historymult;
  }

  public void setSetinfo_rice_initialhistory(int setinfo_rice_initialhistory) {
    this.setinfo_rice_initialhistory = setinfo_rice_initialhistory;
  }

  public void setSetinfo_rice_kmodifier(int setinfo_rice_kmodifier) {
    this.setinfo_rice_kmodifier = setinfo_rice_kmodifier;
  }

  public void setSetinfo_max_samples_per_frame(int setinfo_max_samples_per_frame) {
    this.setinfo_max_samples_per_frame = setinfo_max_samples_per_frame;
  }

  public int[] predictor_coef_table = new int[1024];
  public int[] predictor_coef_table_a = new int[1024];
  public int[] predictor_coef_table_b = new int[1024];
}