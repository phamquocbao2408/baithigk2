package baithigiuaki2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class main {
	private static Document doc;
	private static NodeList nl;

	public static void main(String[] args) {
		Thread thread1 = new Thread(() -> {
			try {
				docFileStudent("kq.xml");

			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		Thread thread2 = new Thread(() -> {
			synchronized (main.class) {
				try {
					main.class.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println(".................");
				nl = doc.getElementsByTagName("student");
				for (int i = 0; i < nl.getLength(); i++) {
					Element studentElement = (Element) nl.item(i);
					String id = studentElement.getAttribute("id");
					String name = getElementTextContent(studentElement, "name");
					String address = getElementTextContent(studentElement, "address");
					String dayOfBirthStr = getElementTextContent(studentElement, "dayOfBirth");
					try {
						Date dayOfBirth = new SimpleDateFormat("dd-MM-yyyy").parse(dayOfBirthStr);
						Student student = new Student(id, name, address, dayOfBirth);

						// Tính toán tuổi
						int age = calculateAge(student.getDateOfBirthl());

						// Mã hoá tuổi
						String encodedAge = encodeAge(age);

						// In thông tin mã hoá
						System.out.println("ID: " + student.getId());
						System.out.println("Tên: " + student.getName());
						System.out.println("Tuổi: " + age + " (" + encodedAge + ")");
						System.out.println("--------------------------");
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		});

		Thread thread3 = new Thread(() -> {
			synchronized (main.class) {
				try {
					main.class.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("Thread 3: Đang kiểm tra tổng các chữ số...");

				for (int i = 0; i < nl.getLength(); i++) {
					Element studentElement = (Element) nl.item(i);
					String dayOfBirthStr = getElementTextContent(studentElement, "dayOfBirth");

					// Tính tổng các chữ số trong ngày tháng năm sinh
					int sumOfDigits = calculateSumOfDigits(dayOfBirthStr);

					// Kiểm tra xem tổng các chữ số có phải là số nguyên tố không
					boolean isPrime = isPrime(sumOfDigits);

					// In kết quả kiểm tra
					System.out.println("Ngày sinh: " + dayOfBirthStr);
					System.out.println("Tổng các chữ số: " + sumOfDigits);
					System.out.println("Số nguyên tố: " + isPrime);
					System.out.println("--------------------------");
				}
			}
		});

		// Khởi động các luồng
		thread1.start();
		thread2.start();
		thread3.start();

		// Chờ các luồng hoàn thành
		try {
			thread1.join();
			thread2.join();
			thread3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Tạo và ghi thông tin vào file kq.xml
		taoFileXMLKetQua("kq.xml");
	}

	private static void docFileStudent(String fileString) throws SAXException, IOException {

		try {
			File file = new File(fileString);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbr = dbf.newDocumentBuilder();

			doc = dbr.parse(file);

			doc.getDocumentElement().normalize();

			NodeList nl = doc.getElementsByTagName("student");
			for (int i = 0; i < nl.getLength(); i++) {
				Element sinhVienElement = (Element) nl.item(i);
				String id = sinhVienElement.getAttribute("id");
				String name = getElementTextContent(sinhVienElement, "name");
				String address = getElementTextContent(sinhVienElement, "address");
				String dayOfBirthStr = getElementTextContent(sinhVienElement, "dayOfBirth");
				try {
					Date dayOfBirth = new SimpleDateFormat("dd-MM-yyyy").parse(dayOfBirthStr);
					Student student = new Student(id, name, address, dayOfBirth);

					System.out.println("ID: " + student.getId());
					System.out.println("Tên: " + student.getName());
					System.out.println("Địa chỉ: " + student.getAddress());
					System.out.println("Ngày sinh: " + dayOfBirthStr);
					System.out.println("--------------------------");

					synchronized (main.class) {
						main.class.notify();
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}

			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private static int calculateAge(Date dob) {
		Calendar today = Calendar.getInstance();
		Calendar dobCal = Calendar.getInstance();
		dobCal.setTime(dob);

		int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
		if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
			age--;
		}
		return age;
	}

	private static String encodeAge(int age) {
		return String.valueOf(age);
	}

	private static int calculateSumOfDigits(String str) {
		int sum = 0;
		for (int i = 0; i < str.length(); i++) {
			if (Character.isDigit(str.charAt(i))) {
				sum += Character.getNumericValue(str.charAt(i));
			}
		}
		return sum;
	}

	private static boolean isPrime(int num) {
		if (num <= 1) {
			return false;
		}
		for (int i = 2; i <= Math.sqrt(num); i++) {
			if (num % i == 0) {
				return false;
			}
		}
		return true;
	}

	private static String getElementTextContent(Element element, String tagName) {
		String content = "";
		if (element.getElementsByTagName(tagName).getLength() > 0) {
			content = element.getElementsByTagName(tagName).item(0).getTextContent();
		}
		return content;
	}

	private static void taoFileXMLKetQua(String tenFile) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();

			Element rootElement = doc.createElement("KetQua");
			doc.appendChild(rootElement);

			for (int i = 0; i < nl.getLength(); i++) {
				Element studentElement = (Element) nl.item(i);
				Element studentInfo = doc.createElement("SinhVien");

				// ID
				String id = studentElement.getAttribute("id");
				Element idElement = doc.createElement("ID");
				idElement.appendChild(doc.createTextNode(id));
				studentInfo.appendChild(idElement);

				// Tên
				String name = getElementTextContent(studentElement, "name");
				Element nameElement = doc.createElement("Ten");
				nameElement.appendChild(doc.createTextNode(name));
				studentInfo.appendChild(nameElement);

				// Địa chỉ
				String address = getElementTextContent(studentElement, "address");
				Element addressElement = doc.createElement("DiaChi");
				addressElement.appendChild(doc.createTextNode(address));
				studentInfo.appendChild(addressElement);

				// Ngày sinh
				String dayOfBirth = getElementTextContent(studentElement, "dayOfBirth");
				Element dobElement = doc.createElement("NgaySinh");
				dobElement.appendChild(doc.createTextNode(dayOfBirth));
				studentInfo.appendChild(dobElement);

				// Tuổi
				String age = calculateAge(new SimpleDateFormat("dd-MM-yyyy").parse(dayOfBirth)) + "";
				Element ageElement = doc.createElement("Tuoi");
				ageElement.appendChild(doc.createTextNode(age));
				studentInfo.appendChild(ageElement);

				// Tổng chữ số
				int sumOfDigits = calculateSumOfDigits(dayOfBirth);
				Element sumElement = doc.createElement("TongChuSo");
				sumElement.appendChild(doc.createTextNode(sumOfDigits + ""));
				studentInfo.appendChild(sumElement);

				// Số nguyên tố
				boolean isPrime = isPrime(sumOfDigits);
				Element primeElement = doc.createElement("SoNguyenTo");
				primeElement.appendChild(doc.createTextNode(isPrime + ""));
				studentInfo.appendChild(primeElement);

				rootElement.appendChild(studentInfo);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(tenFile));
			transformer.transform(source, result);
			System.out.println("File " + tenFile + " đã được tạo thành công.");
		} catch (ParserConfigurationException | IOException | TransformerException | ParseException e) {
			e.printStackTrace();
		}
	}
}
class Input {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Nhập thông tin sinh viên từ bàn phím
        System.out.println("Nhập thông tin sinh viên:");
        System.out.print("ID: ");
        String id = scanner.nextLine();
        System.out.print("Tên: ");
        String name = scanner.nextLine();
        System.out.print("Địa chỉ: ");
        String address = scanner.nextLine();
        System.out.print("Ngày sinh (dd-MM-yyyy): ");
        String dayOfBirth = scanner.nextLine();

        // Tạo file XML và lưu thông tin sinh viên vào đó
        saveStudentToXML(id, name, address, dayOfBirth);
    }

    private static void saveStudentToXML(String id, String name, String address, String dayOfBirth) {
        try {
            // Tạo một tài liệu XML mới
            Document doc = XML.createDocument();

            // Tạo phần tử <student> và các phần tử con của nó
            Element studentElement = doc.createElement("student");
            studentElement.setAttribute("id", id);

            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(name));

            Element addressElement = doc.createElement("address");
            addressElement.appendChild(doc.createTextNode(address));

            Element dayOfBirthElement = doc.createElement("dayOfBirth");
            dayOfBirthElement.appendChild(doc.createTextNode(dayOfBirth));

            // Thêm các phần tử con vào phần tử <student>
            studentElement.appendChild(nameElement);
            studentElement.appendChild(addressElement);
            studentElement.appendChild(dayOfBirthElement);

            // Thêm phần tử <student> vào tài liệu XML
            doc.appendChild(studentElement);

            // Ghi tài liệu XML vào file
            FileWriter writer = new FileWriter("kq.xml");
            XML.transformDOMSourceToStreamResult(doc, writer);
            writer.close();

            System.out.println("Thông tin sinh viên đã được lưu vào file student.xml.");
        } catch (ParserConfigurationException | TransformerException | IOException e) {
            e.printStackTrace();
        }
    }
}