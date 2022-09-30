

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Test
 */
@WebServlet("/Test")
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Test() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String cipherSuite = (String) request.getAttribute("javax.servlet.request.cipher_suite");
		X509Certificate client = null;
		
		if (cipherSuite != null) {
            X509Certificate certChain[] = (X509Certificate[]) 
            	request.getAttribute("javax.servlet.request.X509Certificate");
            if (certChain != null) {
            	client = certChain[0];
            }
		}
		
		StringBuilder message = new StringBuilder();
		message.append("Hi, ");
		
		if (cipherSuite == null) {
			message.append("stranger");
		}
		else {
			
			if (client != null) {
				Principal subjectDN = client.getSubjectDN();
				message.append("'" + subjectDN + "', who uses '" + cipherSuite + "'");
			}
			else {
				message.append("stranger, who uses '" + cipherSuite + "'");
			}
				
		}
		
		message.append("!");
		response.setContentType("text/html");
        
		PrintWriter writer = response.getWriter();        
        
        writer.println("<html>");
        writer.println("<head>");
        
        writer.println("<title>Sample Application Servlet Page</title>");
        writer.println("</head>");
        
        writer.println("<body bgcolor=white>");
        
        writer.println("<table border=\"0\" cellpadding=\"10\">");
        writer.println("<tr>");
        writer.println("<td>");
        writer.println("<h1>" + message + "</h1>");
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");

        writer.println("</body>");
        writer.println("</html>");
        
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
