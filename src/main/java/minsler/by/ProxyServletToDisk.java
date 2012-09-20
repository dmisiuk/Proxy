package minsler.by;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServletToDisk extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("toDiskServlet: connected to servlet");
		String message = "Файл загружен успешно!";
		String err = "Ошибка при чтении файла - файл неопределен";
		resp.setContentType("text/html;charset=utf-8");
		System.out.println(req.getClass());

		PrintWriter out = resp.getWriter();

		String contentType = req.getContentType();
		ServletInputStream in = req.getInputStream();
		DataInputStream dis = new DataInputStream(in);
		// dis.mark(1024);
		// InputStreamReader isr = new InputStreamReader(in);
		// BufferedReader br = new BufferedReader(isr);
		String line;
		int lentghOfInfoContent = 0;
		StringBuilder sb = new StringBuilder();

		// while ((line = br.readLine()) != null && line.length() != 0) {
		// System.out.println(line);
		// lentghOfInfoContent += line.length() + 1;
		// }
		// lentghOfInfoContent++;
		// System.out.println("line|" + line + "|....");
		//
		// dis.reset();
		// // System.out.print("after");
		//
		// int length = req.getContentLength();
		// System.out.println("toDiskServlet: length " + length);
		// System.out.println("toDiskServlet: contentType " + contentType);
		// String boundary = contentType.substring(contentType
		// .indexOf("boundary=") + 9);
		// System.out.println("toDiskServlet: boundary " + boundary);
		//
		// lentghOfInfoContent += boundary.length() + 5;
		// System.out.println("readed byte: " + lentghOfInfoContent);

		byte array[] = new byte[8 * 1024];
		byte preArray[] = new byte[8 * 1024];
		int nread;
		FileOutputStream fos = new FileOutputStream("uploadedByParts.in");
		while ((nread = dis.read(array)) >= 0) {
			// System.out.println("toDiskServlet: nread: " + nread);
			// System.out.println("toDiskServlet: available: " +
			// dis.available());
			String substring = new String(array, 0, nread);
			sb.append(substring);
			System.out.print(substring);
			fos.write(array, 0, nread);
		}
		System.out.println("bytes in sb:" + new String(sb).getBytes().length);
		fos.flush();
		fos.close();
		System.out.println("charter in sb:" + sb.length());
		// System.out.println("toDiskServlet: nread after while: " + nread);
		// byte array[] = new byte[length];
		// int dataRead = 0, totalData = 0;
		//
		// while (totalData < length) {
		// System.out.println("available byte is " + dis.available());
		// dataRead = dis.read(array, totalData, length);
		// totalData += dataRead;
		// System.out.println("total data = " + totalData + "| dataread = "
		// + dataRead);
		// }
		//
		// FileOutputStream fos1 = new FileOutputStream("uploadedfile.in");
		// fos1.write(array);
		// fos1.close();

		out.println("<html>" + "<head>"
				+ "<title>Загрузка файла на сервер</title>" + "</head>"
				+ "<body bgcolor=#fff1df>" + "<h3>" + message + "</h3>"
				+ "</body>" + "</html>");
		out.close();
	}
}
