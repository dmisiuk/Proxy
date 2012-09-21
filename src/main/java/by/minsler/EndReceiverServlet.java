package by.minsler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class EndReceiverServlet extends MainServlet {

	private static Logger logger = Logger.getLogger(EndReceiverServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("toDiskServlet: connected to servlet");

		resp.setContentType("text/html;charset=utf-8");
		resp.setCharacterEncoding("utf-8");
		req.setCharacterEncoding("utf-8");

		PrintWriter out = resp.getWriter();

		String contentType = req.getContentType();
		ServletInputStream in = req.getInputStream();
		DataInputStream dis = new DataInputStream(in);

		int contentLength = req.getContentLength();
		String boundary = contentType.substring(contentType
				.indexOf("boundary=") + 9);

		// set buffer for stream
		byte buffer[] = new byte[8 * 1024];

		boolean isBodyAttachment = false;

		StringBuilder sb = new StringBuilder();
		int nread;

		String contentIdPayload = "Content-ID: Payload-0" + "\n\n";

		int indexFrom = 0;
		int indexContentIdPayload = -1;

		// search index of string "Content-ID: Payload-0\n\n"
		while ((nread = dis.read(buffer)) >= 0) {
			String substring = new String(buffer, 0, nread);
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

		// calculate index of byte start attachment
		int endIndex = indexContentIdPayload + contentIdPayload.length();

		// calculate length of file to save
		int availableBytesForWrite = contentLength
				- sb.substring(0, endIndex).getBytes().length
				- endBoundary.getBytes().length;

		// create file for save attachment
		String homeDir = getServletContext().getRealPath("/");
		File attachmentFile = new File(homeDir,
				"WEB-INF/upload/attachment.part");
		FileOutputStream fos = new FileOutputStream(attachmentFile);

		// start write byte from stream to file
		availableBytesForWrite -= super.writeToOutput(fos,
				sb.substring(endIndex).getBytes(), availableBytesForWrite);

		while ((nread = dis.read(buffer)) >= 0) {
			availableBytesForWrite -= super.writeToOutput(fos, buffer, 0,
					nread, availableBytesForWrite);
		}

		fos.flush();
		fos.close();

		out.println("success");
	}

}
