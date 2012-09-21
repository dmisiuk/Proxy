package minsler.by;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServlet extends HttpServlet {

	URL receiverUrl = null;

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String url = config.getInitParameter("receiverUrl");
		try {
			receiverUrl = new URL(url);
			System.out.println("url initialized");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error initialized url");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("ProxyServlet: connected to servlet");
		resp.setContentType("text/html;charset=utf-8");
		resp.setCharacterEncoding("utf-8");
		req.setCharacterEncoding("utf-8");

		PrintWriter out = resp.getWriter();
		String contentType = req.getContentType();
		DataInputStream dis = new DataInputStream(req.getInputStream());
		// if is multipart then

		int contentLength = req.getContentLength();
		String boundaryOfInputRequest = contentType.substring(contentType
				.indexOf("boundary=") + 9);

		byte array[] = new byte[8 * 1024];
		boolean isBodyAttachment = false;
		StringBuilder sb = new StringBuilder();
		int nread;
		FileOutputStream fosSoapFile = new FileOutputStream(
				"upload/uploaded-soap.xml");
		String contentIdPayload = "Content-ID: Payload-0" + "\n\n";
		int indexFrom = 0;
		int indexContentIdPayload = -1;

		while ((nread = dis.read(array)) >= 0) {
			// System.out.println("start search from:" + indexFrom);
			// System.out.println("toDisk: nread: " + nread);
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

		String endBoundary = "--" + boundaryOfInputRequest + "--" + "\n";
		int endIndex = indexContentIdPayload + contentIdPayload.length();
		int startSoapXml = sb.indexOf("Content-Id: <soappart>\n\n")
				+ "Content-Id: <soappart>\n\n".length();
		int endSaopXml = sb.indexOf("\n--" + boundaryOfInputRequest);
		System.out.println("statSoap: " + startSoapXml + "\nEndSoap: "
				+ endSaopXml);
		fosSoapFile.write(sb.substring(startSoapXml, endSaopXml).getBytes());
		fosSoapFile.flush();
		fosSoapFile.close();

		int availableBytesForWrite = contentLength
				- sb.substring(0, endIndex).getBytes().length
				- endBoundary.getBytes().length;

		HttpURLConnection connection = (HttpURLConnection) receiverUrl
				.openConnection();

		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		// TO-DO boundary
		String boundaryForNewRequest = generateBoundary();

		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundaryForNewRequest);

		String attachmentHeaders = "--" + boundaryForNewRequest + "\n"
				+ "Content-Type: text/plain; charset=\"utf-8\"\n"
				+ "Content-ID: Payload-0\n\n";
		String multipartEnd = "--" + boundaryForNewRequest + "--\n";

		StringBuilder sbNewRequest = new StringBuilder();
		sbNewRequest.append(attachmentHeaders).append(multipartEnd);
		long lentghOfStream = availableBytesForWrite
				+ new String(sbNewRequest).getBytes().length;

		connection.setFixedLengthStreamingMode(lentghOfStream);

		DataOutputStream dos = new DataOutputStream(
				connection.getOutputStream());

		dos.writeBytes(attachmentHeaders);

		availableBytesForWrite -= writeToOutput(dos, sb.substring(endIndex)
				.getBytes(), availableBytesForWrite);

		while ((nread = dis.read(array)) >= 0) {
			// String substring = new String(array, 0, nread);
			// System.out.print(substring);
			// fos.write(array, 0, nread);
			availableBytesForWrite -= writeToOutput(dos, array, 0, nread,
					availableBytesForWrite);

		}

		dos.writeBytes(multipartEnd);
		dos.flush();
		dos.close();
		System.out.println("ProxyServlet: response code from toDisk servlet"
				+ connection.getResponseCode());
		connection.disconnect();
		out.println("success");
	}

	private static String generateBoundary() {
		// TO-DO implement generation boundary
		return "1newpartminslerbyboundary";
	}

	private int writeToOutput(OutputStream out, byte[] b, int availableToWrite)
			throws IOException {
		return this.writeToOutput(out, b, 0, b.length, availableToWrite);
	}

	private int writeToOutput(OutputStream out, byte[] b, int startIndex,
			int endIndex, int availableToWrite) throws IOException {
		if (availableToWrite >= (endIndex - startIndex)) {
			out.write(b, startIndex, endIndex);
			return (endIndex - startIndex);
		} else {
			out.write(b, startIndex, startIndex + availableToWrite);
			return availableToWrite;
		}
	}
}
