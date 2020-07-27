package com.lin.lucene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
public class SimpleLucene {
	/**
	 *
	 * @param indexPath 索引库路径
	 * @param fieldPath 域文件夹路径
	 * @throws IOException
	 */
	public static void createIndex(String indexPath, String fieldPath) throws IOException {
		// 0.索引库的存放位置Directory对象,没有则创建
		Directory directory = FSDirectory.open(new File(indexPath));
		// 注意:索引库保存到内内,IO操作减少。关闭应用索引库失效GG
		// Directory directory = new RAMDirectory();
		// 0.定义分词器(IKAnalyzer)
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		// 1.创建 indexWriter 对象
		IndexWriter indexWriter = new IndexWriter(directory, config);
		// 2.创建field对象,将field添加到document对象中
		File fieldFile = new File(fieldPath);
		if(fieldFile.exists()){
			// 3.遍历当前域路径下文件
			File[] files = fieldFile.listFiles();
			// 4.创建 Document对象
			Document document = new Document();
			for (File file : files) {
				// 获取文件名
				String file_name = file.getName();
				// 创建文件名称域(域名、域值)
				Field fileNameField = new TextField("fileName", file_name, Store.YES);
				// 获取文件大小
				long file_size = FileUtils.sizeOf(file);
				Field fileSizeField = new LongField("fileSize", file_size, Store.YES);
				// 获取文件路径
				String file_path = file.getPath();
				Field filePathField = new StoredField("filePath", file_path);
				// 获取文件内容
				String file_content = FileUtils.readFileToString(file);
				Field fileContentField = new TextField("fileContent", file_content, Store.NO);
				// 5.indexWriter 对象将 document 对象写入索引库,此过程进行索引创建。并将索引和document对象写入(更新)索引库
				document.add(fileNameField);
				document.add(fileSizeField);
				document.add(filePathField);
				document.add(fileContentField);
				indexWriter.addDocument(document);
			}
		}
		indexWriter.close();
	}

	/**
	 *
	 * @param indexPath 索引库路径
	 * @throws IOException
	 */
	public static void searchIndex(String indexPath) throws IOException {
		// 0.索引库的存放位置Directory对象,没有则创建
		Directory directory = FSDirectory.open(new File(indexPath));
		// 1.创建一个indexReader对象,指定Directory对象
		IndexReader indexReader = DirectoryReader.open(directory);
		// 2.创建一个indexsearcher对象,指定IndexReader对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		// 3.创建一个TermQuery对象,指定查询的域和查询的关键词
		Query query = new TermQuery(new Term("fileContent", "java"));
		// 4.执行查询、遍历查询结果并输出
		TopDocs topDocs = indexSearcher.search(query, 10);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			int doc = scoreDoc.doc;
			Document document = indexSearcher.doc(doc);
			// 文件名称
			String fileName = document.get("fileName");
			// 文件内容
			String fileContent = document.get("fileContent");
			// 文件大小
			String fileSize = document.get("fileSize");
			// 文件路径
			String filePath = document.get("filePath");

			System.out.println(String.format("文件名称: %s, 文件大小: %s, 文件路径: %s, 文件内容: %s ", fileName, fileSize, filePath, fileContent).toString());
		}
		indexReader.close();
	}


	public static void showAnalyzer() throws IOException {
		// 1.创建一个标准分析器对象
		Analyzer analyzer = new IKAnalyzer();
		// 2.第一个参数域名,第二参数分析的文本
		TokenStream tokenStream = analyzer.tokenStream("show",
				"武汉java-尚学堂_百万程序员口口相传,武汉 java 北大青鸟Java纯面授课程,Java(计算机编程语言)_百度百科");
		// 3.添加一个引用,可以获得每个关键词
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		// 4.添加一个偏移量的引用,记录了关键词的开始位置以及结束位置
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		// 5.将指针调整到列表的头部
		tokenStream.reset();
		// 遍历关键词列表，通过incrementToken方法判断列表是否结束
		while (tokenStream.incrementToken()) {
			// 关键词的起始位置
			System.out.println("start->" + offsetAttribute.startOffset());
			// 取关键词
			System.out.println(charTermAttribute);
			// 结束位置
			System.out.println("end->" + offsetAttribute.endOffset());
		}
		tokenStream.close();
	}

	public static void main(String[] args) throws IOException {
		createIndex("E:\\index", "E:\\field");
		searchIndex("E:\\index");
		showAnalyzer();
	}
}
