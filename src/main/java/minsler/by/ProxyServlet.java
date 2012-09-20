package minsler.by;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String message = "Файл загружен успешно!";
		String err = "Ошибка при чтении файла - файл неопределен";
		resp.setContentType("text/html;charset=utf-8");

		PrintWriter out = resp.getWriter();

		String contentType = req.getContentType();
		DataInputStream dis = new DataInputStream(req.getInputStream());

		int length = req.getContentLength();
		byte array[] = new byte[length];
		int dataRead = 0, totalData = 0;

		while (totalData < length) {
			dataRead = dis.read(array, totalData, length);
			totalData += dataRead;
		}

		String data = new String(array);
		int lastIndex = contentType.lastIndexOf("=");
		String boundary = contentType.substring(lastIndex + 1,
				contentType.length());

		int position = data.indexOf("filename=\"");
		int pz_s = position + 10;
		position = data.indexOf("\n", position) + 1;
		int pz_e = position - 4;

		String FileName = "";

		if ((pz_s > 0) && (pz_s < pz_e)) {
			FileName = "moved";
			char[] c = new char[pz_e - pz_s + 1];
			for (int i = pz_s; i <= pz_e; i++)
				c[i - pz_s] = data.charAt(i);
			FileName = String.valueOf(c, 0, c.length);
			pz_s = FileName.indexOf('\\');
			while (pz_s > 0) {
				FileName = FileName.substring(pz_s + 1, FileName.length());
				pz_s = FileName.indexOf('\\');
			}
		}

		if (FileName.length() > 0) {
			position = data.indexOf("\n", position) + 1;
			position = data.indexOf("\n", position) + 1;
			String fileData = data.substring(position,
					data.indexOf(boundary, position) - 4);
			if (fileData.length() > 0) {
				FileOutputStream fos = new FileOutputStream("c:\\" + FileName);
				fos.write(array, position, fileData.length());
				fos.close();
			} else
				message = err;
		} else
			message = err;
		dis.close();
		out.println("<html>" + "<head>"
				+ "<title>Загрузка файла на сервер</title>" + "</head>"
				+ "<body bgcolor=#fff1df>" + "<h3>" + message + "</h3>"
				+ "</body>" + "</html>");
		out.close();
	}
}
