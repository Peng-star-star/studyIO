package cn.study.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class TestFileIO {

	// FileInputStream->FileOutputStream
	public void testFileStream() {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(TestFileIO.class.getResource("/read.txt").getFile());
			fos = new FileOutputStream(TestFileIO.class.getResource("/write.txt").getFile());
			byte[] buf = new byte[1024];
			int hasRead = 0;
			// read参数为数据，返回值为数据长度
			while ((hasRead = fis.read(buf)) > 0) {
				fos.write(buf, 0, hasRead);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// FileReader->FileWriter
	//中文会出现乱码
	public void testFileReaderWriter() {
		FileReader fr = null;
		FileWriter fw = null;
		try {
			fr = new FileReader(TestFileIO.class.getResource("/read.txt").getFile());
			fw = new FileWriter(TestFileIO.class.getResource("/write.txt").getFile());
			char[] buf = new char[32];
			int hasRead = 0;
			while ((hasRead = fr.read(buf)) > 0) {
				fw.write(buf, 0, hasRead);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// FileInputStream->InputStreamReader->FileWriter
	//中文乱码
	public void testInputStreamReader() {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		FileWriter fw = null;
		try {
			fis = new FileInputStream(TestFileIO.class.getResource("/read.txt").getFile());
			isr = new InputStreamReader(fis,"GBK");
			fw = new FileWriter(TestFileIO.class.getResource("/write.txt").getFile(),true);
			char[] buf = new char[32];
			int hasRead = 0;
			while ((hasRead = isr.read(buf)) > 0) {
				System.err.println(Arrays.toString(buf));
				fw.write(buf, 0, hasRead);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// FileReader->OutputStreamWriter->FileOutputStream
	//中文乱码
	public void testOutputStreamWriter() {
		FileReader fr = null;
		OutputStreamWriter osw = null;
		FileOutputStream fos = null;
		try {
			fr = new FileReader(TestFileIO.class.getResource("/read.txt").getFile());
			fos = new FileOutputStream(TestFileIO.class.getResource("/write.txt").getFile(),true);
			osw = new OutputStreamWriter(fos,"UTF-8");
			char[] buf = new char[32];
			int hasRead = 0;
			while ((hasRead = fr.read(buf)) > 0) {
				System.err.println(Arrays.toString(buf));
				osw.write(buf, 0, hasRead);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// FileInputStream->BufferedInputStream->BufferedOutputStream->FileOutputStream
	private void testBufferedInputStream() {
		FileInputStream inputStream = null;
		BufferedInputStream bufferedInputStream = null;

		FileOutputStream outputStream = null;
		BufferedOutputStream bufferedOutputStream = null;

		try {
			inputStream = new FileInputStream(TestFileIO.class.getResource("/read.txt").getFile());
			bufferedInputStream = new BufferedInputStream(inputStream);

			outputStream = new FileOutputStream(TestFileIO.class.getResource("/write.txt").getFile(),true);
			bufferedOutputStream = new BufferedOutputStream(outputStream);

			byte[] b = new byte[1024];
			int length = 0; // 代表实际读取的字节数
			while ((length = bufferedInputStream.read(b)) != -1) {
				// length 代表实际读取的字节数
				bufferedOutputStream.write(b, 0, length);
			}
			// 缓冲区的内容写入到文件
			bufferedOutputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedOutputStream != null) {
				try {
					bufferedOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// BufferedReader->BufferedWriter
	public void testBufferd() {
		BufferedReader br = null;
		FileReader fr = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fr = new FileReader(TestFileIO.class.getResource("/read.txt").getFile());
			br = new BufferedReader(fr);
			fw = new FileWriter(TestFileIO.class.getResource("/write.txt").getFile(),true);
			bw = new BufferedWriter(fw);
			String line = null;
			while ((line = br.readLine()) != null) {
				bw.write(line);
			}
			bw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		TestFileIO test = new TestFileIO();
		//test.testFileStream();
		//test.testFileReaderWriter();
		//test.testInputStreamReader();
		//test.testOutputStreamWriter();
		//test.testBufferedInputStream();
		test.testBufferd();
	}
}
