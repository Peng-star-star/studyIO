package cn.study.netty;

import java.nio.charset.Charset;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

public class StudyByteBuf {
	
	/*
	 * 参考下面网站的例子，做测试
	 * https://www.w3cschool.cn/essential_netty_in_action/
	 */
	
	
	public static void main(String[] args) {
		compositeByteBuf();
	}
	
	/***********************Netty字节数据的容器ByteBuf*************************/
	
	//HEAP BUFFER(堆缓冲区)
	public static void backingArray(){
		//ByteBuf heapBuf = ...;
		//if (heapBuf.hasArray()) {                //1
		//    byte[] array = heapBuf.array();        //2
		//    int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();                //3
		//    int length = heapBuf.readableBytes();//4
		//    handleArray(array, offset, length); //5
		//}
	}
	//DIRECT BUFFER(直接缓冲区)
	public static void directBuffer(){
		//ByteBuf directBuf = ...
		//if (!directBuf.hasArray()) {            //1
		//	int length = directBuf.readableBytes();//2
		//	byte[] array = new byte[length];    //3
		//	directBuf.getBytes(directBuf.readerIndex(), array);        //4    
		//	handleArray(array, 0, length);  //5
		//}
	}
	//COMPOSITE BUFFER(复合缓冲区)
	public static void compositeByteBuf(){
		CompositeByteBuf messageBuf = Unpooled.compositeBuffer(); 
		ByteBuf headerBuf = Unpooled.copiedBuffer("header buf", Charset.forName("UTF-8")); // 可以支持或直接
		ByteBuf bodyBuf = Unpooled.copiedBuffer("body buf", Charset.forName("UTF-8")); // 可以支持或直接
		messageBuf.addComponents(headerBuf, bodyBuf);
		// ....
		messageBuf.removeComponent(0); // 移除头    //2

		for (int i = 0; i < messageBuf.numComponents(); i++) {                        //3
		    System.out.println(messageBuf.component(i).toString(Charset.forName("UTF-8")));
		}
	}
	//COMPOSITE BUFFER(复合缓冲区)读取
	public static void compositeByteBufRead(){
		//等同于直接缓冲区读取
	}
	
	/***********************Netty字节级别的操作*************************/
	
	//随机访问索引
	public static void accessData(){
		ByteBuf buffer = Unpooled.buffer(10);
		for (int i = 0; i < buffer.capacity(); i++) {
		    byte b = buffer.getByte(i);
		    System.out.println(b);
		}
	}
	
	//可丢弃字节的字节
	public static void discardReadBytes(){
		ByteBuf buffer = Unpooled.buffer(10);
		byte[] a = {1,2,3,4,5,6};
		//写入数据，writerIndex为6
		buffer.writeBytes(a);
		System.out.println(Arrays.toString(getBytes(buffer)));
		
		//将readerIndex设置到3处
		buffer.readerIndex(3);
		//将readerIndex与writerIndex之间数据前移，readerIndex=0；writerIndex=3；
		//结果[4, 5, 6, 4, 5, 6, 0, 0, 0, 0]
		buffer.discardReadBytes();
		System.out.println(Arrays.toString(getBytes(buffer)));
		System.out.println(buffer.readerIndex());
		System.out.println(buffer.writerIndex());
	}
	
	//可读字节
	public static void readAllData(){
		ByteBuf buffer = Unpooled.buffer(10);
		byte[] a = {1,2,3,4,5,6};
		//写入数据，writerIndex为6
		buffer.writeBytes(a);
		System.out.println(Arrays.toString(getBytes(buffer)));
		//readerIndex=writerIndex
		while (buffer.isReadable()) {
		    System.out.println(buffer.readByte());
		}
	}
	
	public static void writeData(){
		ByteBuf buffer = Unpooled.buffer(10);
		byte[] a = {1,2,3,4,5,6};
		//写入数据，writerIndex为6
		buffer.writeBytes(a);
		//capacity-writerIndex=10-6=4
		System.out.println(buffer.writableBytes());
		while (buffer.writableBytes() >= 4) {
		    buffer.writeInt(1);
		}
		System.out.println(Arrays.toString(getBytes(buffer)));
	}
	
	//索引管理
	public static void mark(){
		ByteBuf buffer = Unpooled.buffer(10);
		byte[] a = {1,2,3,4,5,6};
		//写入数据，writerIndex为5
		buffer.writeBytes(a);
		
		//mark=writerIndex= 6
		buffer.markWriterIndex();
		System.out.println(buffer.writerIndex());
		//writerIndex= 7;mark=6;
		buffer.writeByte(1);
		System.out.println(buffer.writerIndex());
		//mark=writerIndex= 6
		buffer.resetWriterIndex();
		System.out.println(buffer.writerIndex());
	}
	
	public static void clear(){
		//他只是重置了索引，而没有内存拷贝
		ByteBuf buffer = Unpooled.buffer(10);
		byte[] a = {1,2,3,4,5,6};
		//写入数据，writerIndex为5
		buffer.writeBytes(a);
		
		buffer.clear();
		System.err.println(buffer.readerIndex());
		System.err.println(buffer.writerIndex());
		System.err.println(Arrays.toString(getBytes(buffer)));
	}
	
	//查询操作
	//可以转为byte[]再用ascii找
	public static void usingByteBufProcessorToFind(){
		ByteBuf buffer = Unpooled.buffer(10);
		byte[] a = {1,2,3,4,5,6};
		//写入数据，writerIndex为5
		buffer.writeBytes(a);
		
		int index = buffer.forEachByte(ByteBufProcessor.FIND_CR);
		
	}
	//衍生的缓冲区
	public static void  sliceAByteBuf(){
		//duplicate(), slice(), slice(int, int),readOnly(), 和 order(ByteOrder) 内部数据存储
		//如果需要已有的缓冲区的全新副本，使用 copy() 或者 copy(int, int)
		Charset utf8 = Charset.forName("UTF-8");
		ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8); //1

		ByteBuf sliced = buf.slice(0, 14);          //2
		System.out.println(sliced.toString(utf8));  //3

		buf.setByte(0, (byte) 'J');                 //4
		System.out.println("修改原数据，slice后的数据也会修改："+(buf.getByte(0) == sliced.getByte(0)));
	}
	
	public static void copyingAByteBuf(){
		Charset utf8 = Charset.forName("UTF-8");
		ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);     //1

		ByteBuf copy = buf.copy(0, 14);               //2
		System.out.println(copy.toString(utf8));      //3

		buf.setByte(0, (byte) 'J');                   //4
		System.out.println("修改原数据，copy后的数据不会修改："+(buf.getByte(0) != copy.getByte(0)));
	}
	
	//获取全部的数据用byte表示
	public static byte[] getBytes(ByteBuf buffer){
		return buffer.array();
	}
	
	
	//其他方法
	//maxCapacity 最大值 int.MaxValue
	//hasArray  堆缓冲区||直接缓冲区
	
	/***********************Netty之ByteBuf 分配*************************/
	
	//ByteBufAllocator
	public static void byteBufAllocator(){
		//Channel channel = ...;
		//ByteBufAllocator allocator = channel.alloc(); //1
		//....
		//ChannelHandlerContext ctx = ...;
		//ByteBufAllocator allocator2 = ctx.alloc(); //2
		//...
	}
	
	//Unpooled （非池化）缓存
	public static void unpooled(){
		//Unpooled.buffer(10);堆缓冲区
		//Unpooled.directBuffer();直接缓冲区
		//Unpooled.copiedBuffer();byte,string,char转bytebuf
	}
	//ByteBufUtil
	public static void byteBufUtil(){
		byte[] a = {1,2,3,4,5,6};
		String hexStr = ByteBufUtil.hexDump(a);//16进制字符串表示
		System.out.println(hexStr);
		//比较 ByteBuf实例是否相等，不知道比较的什么？
		//ByteBufUtil.equals(bufferA, bufferB)
	} 
	
	/***********************Netty引用计数器*************************/
	public static void referenceCounting(){
		//buffer.refCnt();获取计数器个数
		//buffer.release();释放一个
	}
}
