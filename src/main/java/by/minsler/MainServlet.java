package by.minsler;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected int writeToOutput(OutputStream out, byte[] b, int availableToWrite)
			throws IOException {
		return this.writeToOutput(out, b, 0, b.length, availableToWrite);
	}

	protected int writeToOutput(OutputStream out, byte[] b, int startIndex,
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
