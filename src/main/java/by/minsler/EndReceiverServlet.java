package by.minsler;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EndReceiverServlet extends MainServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("toDiskServlet: connected to servlet");

		resp.setContentType("text/html;charset=utf-8");
		resp.setCharacterEncoding("utf-8");
		req.setCharacterEncoding("utf-8");
		// System.out.println(req.getClass());

		PrintWriter out = resp.getWriter();

		String contentType = req.getContentType();
		ServletInputStream in = req.getInputStream();
		DataInputStream dis = new DataInputStream(in);

		int contentLength = req.getContentLength();
		System.out.println("toDiskServlet: length " + contentLength);
		System.out.println("toDiskServlet: contentType " + contentType);
		String boundary = contentType.substring(contentType
				.indexOf("boundary=") + 9);
		System.out.println("toDiskServlet: boundary " + boundary);

		// System.out.println("readed byte: " + lentghOfInfoContent);

		byte array[] = new byte[8 * 1024];
		boolean isBodyAttachment = false;

		StringBuilder sb = new StringBuilder();
		int nread;
		FileOutputStream fos = new FileOutputStream("upload/attachment.part");
		String contentIdPayload = "Content-ID: Payload-0" + "\n\n";
		int indexFrom = 0;
		int indexContentIdPayload = -1;

		while ((nread = dis.read(array)) >= 0) {
			System.out.println("start search from:" + indexFrom);
			System.out.println("toDisk: nread: " + nread);
			// end of stream?
			String substring = new String(array, 0, nread);
			// System.out.println("substring: " + substring);
			sb.append(substring);
			if ((indexContentIdPayload = sb
					.indexOf(contentIdPayload, indexFrom)) >= 0) {
				isBodyAttachment = true;
				break;
			} else {
				indexFrom = sb.length() - contentIdPayload.length() - 1;
			}
		}

		String endBoundary = "--" + boundary + "--" + "\n";
		int endIndex = indexContentIdPayload + contentIdPayload.length();
		int availableBytesForWrite = contentLength
				- sb.substring(0, endIndex).getBytes().length
				- endBoundary.getBytes().length;

		availableBytesForWrite -= super.writeToOutput(fos,
				sb.substring(endIndex).getBytes(), availableBytesForWrite);

		while ((nread = dis.read(array)) >= 0) {
			availableBytesForWrite -= super.writeToOutput(fos, array, 0, nread,
					availableBytesForWrite);
		}
		System.out.println("isBodyAttachment: " + isBodyAttachment);
		System.out.println("Index: " + indexContentIdPayload);

		fos.flush();
		fos.close();

		out.println("<html>" + "<head>"
				+ "<title>Загрузка файла на сервер</title>" + "</head>"
				+ "<body bgcolor=#fff1df>" + "<h3>" + "успешно" + "</h3>"
				+ "</body>" + "</html>");
		out.close();
	}

}
