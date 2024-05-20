package Controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import model.ProductModel;
import model.game;

/**
 * Servlet implementation class AddGame
 */
@WebServlet("/AddGame")
@MultipartConfig()
public class UploadGame extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String SAVE_DIR = "img";
	static ProductModel GameModels = new ProductModelDM();
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

	// Mappa dei magic numbers per le immagini consentite
	private static final Map<String, byte[]> MAGIC_NUMBERS = new HashMap<>();

	static {
	    MAGIC_NUMBERS.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
	    MAGIC_NUMBERS.put("jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
	    MAGIC_NUMBERS.put("png", new byte[]{(byte) 0x89, 'P', 'N', 'G'});
	    MAGIC_NUMBERS.put("gif", new byte[]{'G', 'I', 'F'});
	}
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	LocalDateTime now = LocalDateTime.now();
	
	
    public UploadGame() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		out.write("Error: GET method is used but POST method is required");
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Collection<?> games = (Collection<?>) request.getSession().getAttribute("games");
		String savePath = request.getServletContext().getRealPath("") + File.separator + SAVE_DIR;
		game g1 = new game();
		/*
		File fileSaveDir = new File(savePath);
		
		 * if (!fileSaveDir.exists()) { fileSaveDir.mkdir(); }
		 */
		String fileName= null;
		String message = "upload =\n";
		if (request.getParts() != null && request.getParts().size() > 0) {
			for (Part part : request.getParts()) {
				fileName = extractFileName(part);
			
				if (fileName != null && !fileName.equals("")) {
					// Validazione dell'estensione del file
	                if (isAllowedExtension(fileName)) {
	                    // Validazione del contenuto del file
	                    if (isValidFileContent(part, fileName)) {
	                        part.write(savePath + File.separator + fileName);
	                        g1.setImg(fileName);
	                        message = message + fileName + "\n";
	                    } else {
	                        request.setAttribute("error", "Errore: Il contenuto del file non è valido");
	                    }
	                } else {
	                    request.setAttribute("error", "Errore: Estensione del file non consentita");
	                }
				} else {
					request.setAttribute("error", "Errore: Bisogna selezionare almeno un file");
				}
			}
		}
		
		g1.setName(request.getParameter("nomeGame"));
		g1.setYears(request.getParameter("years"));
		g1.setAdded(dtf.format(now));
		g1.setQuantity(Integer.valueOf(request.getParameter("quantita")));
		g1.setPEG(Integer.valueOf(request.getParameter("PEG")));
		g1.setIva(Integer.valueOf(request.getParameter("iva")));
		g1.setGenere(request.getParameter("genere"));
		g1.setDesc(request.getParameter("desc"));
		g1.setPrice(Float.valueOf(request.getParameter("price")));
		
		try {
			GameModels.doSave(g1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//request.setAttribute("message", message);
		request.setAttribute("stato", "success!");
		
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/gameList?page=admin&sort=added DESC");
		dispatcher.forward(request, response);
	}
	private String extractFileName(Part part) {
		// content-disposition: form-data; name="file"; filename="file.txt"
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}
	private boolean isAllowedExtension(String fileName) {
	    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	    return ALLOWED_EXTENSIONS.contains(fileExtension);
	} 

	private boolean isValidFileContent(Part part, String fileName) {
	    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	    byte[] expectedMagicNumber = MAGIC_NUMBERS.get(fileExtension);

	    if (expectedMagicNumber == null) {
	        return false;
	    }

	    try (InputStream inputStream = part.getInputStream()) {
	        byte[] fileMagicNumber = new byte[expectedMagicNumber.length];
	        if (inputStream.read(fileMagicNumber) != fileMagicNumber.length) {
	            return false;
	        }
	        return Arrays.equals(fileMagicNumber, expectedMagicNumber);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	

}
