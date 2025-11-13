package net.devstudy.ishop.testenv;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class TestDataGenerator {
	private static final String JDBC_URL = "jdbc:postgresql://localhost/ishop";
	private static final String JDBC_USERNAME = "ishop";
	private static final String JDBC_PASSWORD = "password";

	private static final String IMG_PATH = "external/test-data/";
	private static final String MEDIA_PATH = "src/main/webapp/media/";

	private static final boolean useImageLinkInsteadOfCopy = true;

	static final String[] E_BOOK_PRODUCER = { "PocketBook", "Amazon", "AirBook", "EvroMedia" };
	static final String[] MP3_PLAYER_PRODUCER = { "Apple", "FiiO", "Jeka", "Sony", "Transcend" };
	static final String[] PHONE_PRODUCER = { "Nokia", "LG", "Gigabyte", "Samsung", "Philips", "Prestigio" };
	static final String[] SMARTPHONE_PRODUCER = { "Apple", "Nokia", "Prestigio", "Lenovo", "Sony", "Samsung", "LG", "Huawei", "HTC", "Fly" };
	static final String[] SMARTWATCH_PRODUCER = { "Apple", "AirOn", "FixiTime", "Samsung" };
	static final String[] NOTEBOOK_PRODUCER = { "Acer", "Asus", "Apple", "Dell", "HP", "Lenovo" };
	static final String[] TABLET_PRODUCER = { "Acer", "Asus", "Apple", "Dell", "Prestigio", "Lenovo", "Sony", "Samsung", "PocketBook" };

	static final String[] E_BOOK_DESC = { "Monitor diagonal {6,12,1}\"", "[Matrix type: E lnk;Matrix type: E lnk Pearl;Matrix type: E lnk Carta]",
			"[Resolution: 800x600;Resolution: 1600x1200;Resolution: 1024x758;Resolution: 1200x800;Resolution: 800x600]", "Memory {4,12,1} GB", "Weight {155,200,5} g" };
	static final String[] E_BOOK_DESC_OPTIONS = {};

	static final String[] MP3_PLAYER_DESC = { "Memory {4,20,4} Gb", "Weight {20,100,3} g", "[Silver;Blue;Orange;White;Black]", "MP3" };
	static final String[] MP3_PLAYER_DESC_OPTIONS = { "WAV", "OGG", "WMA", "MPEG-4", "AMV", "AVI", "SD slot", "FM receiver", "Dictophone", "USB", "Bluetooth" };

	static final String[] PHONE_DESC = { "Monitor diagonal {1.8,3,0.2}\"", "[Camera: 1Mp;Camera: 1.8Mp;Camera: 2.2Mp;Camera: 2.6Mp]", "RAM {16,64,16} Mb", "{1000,2000,100} mA/h",
			"[Silver;Blue;Orange;White;Black]", "Weight {60,400,20} g" };
	static final String[] PHONE_DESC_OPTIONS = { "2 Sim cards", "Dictophone", "USB", "Bluetooth", "SD" };

	static final String[] SMARTPHONE_DESC = { "Monitor diagonal {3.2,6,0.6}\"", "[Camera: 2.2Mp;Camera: 3.2Mp;Camera: 4.0Mp;Camera: 3.0Mp]", "[RAM: 512 Mb;RAM: 1 Gb;RAM: 2 Gb;RAM: 4 Gb]",
			"[Silver;Blue;Orange;White;Black]", "{1000,2000,100} mA/h", "Weight {320,800,40} g" };
	static final String[] SMARTPHONE_DESC_OPTIONS = { "2 Sim cards", "FM receiver", "Dictophone", "USB", "Bluetooth", "WiFi", "GPS", "3G" };

	static final String[] SMARTWATCH_DESC = { "Monitor diagonal {1.1,1.8,0.1}\"", "[RAM: 512 Mb;RAM: 256 Mb;RAM: 512 Mb;RAM: 1 Gb]", "[Silver;Blue;Orange;White;Black]", "{1000,2000,100} mA/h",
			"Weight {60,300,40} g" };
	static final String[] SMARTWATCH_DESC_OPTIONS = { "Dictophone", "USB", "Bluetooth", "WiFi", "GPS" };

	static final String[] NOTEBOOK_DESC = { "Monitor {11,23,1}\"", "[Intel Core I7 ({2.0, 3.2, 0.2} GHz);Intel Core I5 ({2.0, 2.8, 0.2} GHz);Intel Core I3 ({1.6, 2.6, 0.2} GHz)]", "RAM {1,16,1} GB",
			"HDD {60,800,10} GB", "Weight {2.4, 6.6, 0.2} kg" };
	static final String[] NOTEBOOK_DESC_OPTIONS = { "Intel HD Graphics", "DVD+/-RW", "LAN", "WiFi", "Bluetooth", "Webcam", "USB", "HDMI" };

	static final String[] TABLET_DESC = { "Monitor {7,10,1}\"", "RAM {1,4,1} GB", "HDD {60,800,10} GB", "[Silver;Blue;Orange;White;Black]", "Weight {60,300,40} g" };
	static final String[] TABLET_DESC_OPTIONS = { "3G", "WiFi", "Bluetooth", "Webcam", "USB", "HDMI", "GPS" };

	public static void main(String[] args) throws Exception {
		List<Category> categories = loadCategories();
		List<Producer> producers = getProducers(categories);
		clearMediaDir();
		try (Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
			c.setAutoCommit(false);
			clearDb(c);
			Map<String, Integer> producerIdMap = insertProducers(c, producers);
			Map<String, Integer> categoryIdMap = insertCategories(c, categories);
			insertProducts(c, categories, categoryIdMap, producerIdMap);
			c.commit();
		} catch (SQLException e) {
			if (e.getNextException() != null) {
				e.getNextException().printStackTrace();
			} else {
				e.printStackTrace();
			}
		}
		System.out.println("Completed");
	}

	private static List<Category> loadCategories() {
		List<Category> list = new ArrayList<>();
		File imgDir = new File(IMG_PATH);
		if (!imgDir.exists() || !imgDir.isDirectory()) {
			throw new IllegalArgumentException("Directory " + imgDir.getAbsolutePath() + " not found or not directory!");
		}
		for (File file : imgDir.listFiles()) {
			list.add(new Category(file.getName().replace(".jpg", ""), file));
		}
		Collections.sort(list);
		return list;
	}

	private static List<Producer> getProducers(List<Category> categories) {
		Map<String, Producer> map = new HashMap<>();
		for (Category category : categories) {
			for (Producer producer : category.producers) {
				Producer p = map.get(producer.name);
				if (p == null) {
					map.put(producer.name, producer);
				} else {
					map.put(producer.name, new Producer(p.name, p.productCount + producer.productCount));
				}
			}
		}
		return new ArrayList<>(map.values());
	}

	private static void clearDb(Connection c) throws SQLException {
		try (Statement st = c.createStatement()) {
			st.executeUpdate("delete from order_item");
			st.executeUpdate("delete from \"order\"");
			st.executeUpdate("delete from account");
			st.executeUpdate("delete from product");
			st.executeUpdate("delete from category");
			st.executeUpdate("delete from producer");
		}
		System.out.println("Db cleared");
	}

	private static void clearMediaDir() {
		if (!useImageLinkInsteadOfCopy) {
			File dir = new File(MEDIA_PATH);
			if (!dir.exists() || !dir.isDirectory()) {
				throw new IllegalArgumentException("Directory " + dir.getAbsolutePath() + " not found or not directory!");
			}
			int count = 0;
			for (File file : dir.listFiles()) {
				if (file.delete()) {
					count++;
				}
			}
			System.out.println("Removed " + count + " image files");
		}
	}

	private static Map<String, Integer> insertProducers(Connection c, List<Producer> producers) throws Exception {
		Map<String, Integer> idMap = new HashMap<>();
		int i = 1;
		try (PreparedStatement ps = c.prepareStatement("insert into producer values (?,?,?)")) {
			for (Producer producer : producers) {
				idMap.put(producer.name, i);
				ps.setInt(1, i++);
				ps.setString(2, producer.name);
				ps.setInt(3, producer.productCount);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		System.out.println("Inserted " + producers.size() + " producers");
		return idMap;
	}

	private static Map<String, Integer> insertCategories(Connection c, List<Category> categories) throws Exception {
		Map<String, Integer> idMap = new HashMap<>();
		int i = 1;
		try (PreparedStatement ps = c.prepareStatement("insert into category values (?,?,?,?)")) {
			for (Category category : categories) {
				idMap.put(category.name, i);
				ps.setInt(1, i++);
				ps.setString(2, capitalize(category.name));
				ps.setString(3, "/" + category.name.toLowerCase().trim());
				ps.setInt(4, category.getProductCount());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		System.out.println("Inserted " + categories.size() + " categories");
		return idMap;
	}

	private static String capitalize(String name) {
		if (name == null) {
			return null;
		} else if (name.length() == 1) {
			return name.toUpperCase();
		} else {
			return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
		}
	}

	private static void insertProducts(Connection c, List<Category> categories, Map<String, Integer> categoryIdMap, Map<String, Integer> producerIdMap) throws SQLException, IOException {
		List<Product> products = generateProducts(categories, categoryIdMap, producerIdMap);
		int i = 278009;
		try (PreparedStatement ps = c.prepareStatement("insert into product values (?,?,?,?,?,?,?)")) {
			for (Product product : products) {
				ps.setInt(1, i);
				ps.setString(2, product.name);
				ps.setString(3, generateProductDescription(product.category));
				ps.setString(4, product.imageLink);
				ps.setInt(5, RANDOM.nextInt(300) * 10);
				ps.setInt(6, product.idCategory);
				ps.setInt(7, product.idProducer);
				ps.addBatch();
				i += RANDOM.nextInt(10) + 1;
			}
			ps.executeBatch();
		}
		System.out.println("Inserted " + products.size() + " products");
	}

	private static List<Product> generateProducts(List<Category> categories, Map<String, Integer> categoryIdMap, Map<String, Integer> producerIdMap) throws IOException {
		List<Product> products = new ArrayList<>();
		for (Category category : categories) {
			for (Producer producer : category.producers) {
				int idProducer = producerIdMap.get(producer.name);
				int idCategory = categoryIdMap.get(category.name);
				for (int i = 0; i < producer.productCount; i++) {
					String name = generateProductName(producer.name);
					String imageLink = getProductImageLink(category);
					products.add(new Product(category.name, idCategory, idProducer, name, imageLink));
				}
			}
		}
		Collections.shuffle(products);
		return products;
	}

	private static String getProductImageLink(Category category) throws IOException {
		String productImageName = getImageFileName(category);
		File dest = new File(MEDIA_PATH + productImageName);
		if (!dest.exists()) {
			Files.copy(Paths.get(category.imageFile.toURI()), Paths.get(dest.toURI()));
		}
		return "/media/" + productImageName;
	}

	private static String getImageFileName(Category category) {
		if (useImageLinkInsteadOfCopy) {
			StringBuilder fileName = new StringBuilder();
			for (int i = 0; i < category.name.length(); i++) {
				fileName.append(Integer.toHexString((int) category.name.charAt(i)));
			}
			return fileName.toString() + ".jpg";
		} else {
			return UUID.randomUUID().toString().replace("-", "") + ".jpg";
		}
	}

	private static String generateProductName(String producerName) {
		StringBuilder productCode = new StringBuilder();
		for (int i = RANDOM.nextInt(2) + 1; i >= 0; i--) {
			productCode.append((char) ('A' + RANDOM.nextInt(22)));
		}
		for (int i = RANDOM.nextInt(5) + 3; i >= 0; i--) {
			productCode.append(String.valueOf(RANDOM.nextInt(10)));
		}
		return producerName + " " + productCode;
	}

	private static String generateProductDescription(String categoryName) {
		String staticFieldDescName = categoryName.toUpperCase().replace("-", "_") + "_DESC";
		String staticFieldDescOptionsName = categoryName.toUpperCase().replace("-", "_") + "_DESC_OPTIONS";
		try {
			String[] desc = {};
			String[] options = {};
			Field fieldDesc = TestDataGenerator.class.getDeclaredField(staticFieldDescName);
			desc = (String[]) fieldDesc.get(TestDataGenerator.class);
			try {
				Field fieldDescOptions = TestDataGenerator.class.getDeclaredField(staticFieldDescOptionsName);
				options = (String[]) fieldDescOptions.get(TestDataGenerator.class);
			} catch (NoSuchFieldException e) {
				System.err.println(staticFieldDescName + "field not found.");
			}
			return new ProductDescriptionGenerator(desc, options).generateDesc();
		} catch (NoSuchFieldException e) {
			System.err.println(staticFieldDescName + " field not found for category: " + categoryName);
			return "";
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 
	 * 
	 * @author devstudy
	 * @see http://devstudy.net
	 */
	private static class ProductDescriptionGenerator {
		private final String[] patterns;
		private final String[] options;

		ProductDescriptionGenerator(String[] patterns, String[] options) {
			super();
			this.patterns = patterns;
			this.options = options;
		}

		String generateDesc() {
			StringBuilder result = new StringBuilder();
			for (String pattern : patterns) {
				pattern = handleChoice(pattern);
				String value = handleRandomVariable(pattern);
				result.append(value).append(" / ");
			}
			if (options.length > 0) {
				appendOptions(result, options);
			} else {
				result.deleteCharAt(result.length() - 1);
				result.deleteCharAt(result.length() - 1);
				result.deleteCharAt(result.length() - 1);
			}
			return result.toString();
		}

		private String handleChoice(String pattern) {
			if (pattern.startsWith("[") && pattern.endsWith("]")) {
				String[] variants = pattern.replace("[", "").replace("]", "").split(";");
				return variants[RANDOM.nextInt(variants.length)].trim();
			} else {
				return pattern;
			}
		}

		private String handleRandomVariable(String pattern) {
			int start = pattern.indexOf('{');
			int end = pattern.indexOf('}', start);
			if (start != -1 && end != -1) {
				String var = pattern.substring(start + 1, end).trim();
				String[] params = var.replace("{", "").replace("}", "").split(",");
				return pattern.replace("{" + var + "}", generateRandom(params));
			} else {
				return pattern;
			}
		}

		private String generateRandom(String[] params) {
			Set<Double> variants = new HashSet<>();
			double min = Double.parseDouble(params[0].trim());
			double max = Double.parseDouble(params[1].trim());
			double step = Double.parseDouble(params[2].trim());
			for (double v = min; v <= max; v += step) {
				variants.add(v);
			}
			variants.add(max);

			Double[] array = variants.toArray(new Double[] {});
			Double randomValue = array[RANDOM.nextInt(array.length)];
			return isDouble(params) ? new DecimalFormat("#0.0").format(randomValue) : String.valueOf(randomValue.intValue());
		}

		private boolean isDouble(String[] params) {
			for (String param : params) {
				if (param.contains(".")) {
					return true;
				}
			}
			return false;
		}

		private void appendOptions(StringBuilder result, String[] options) {
			int count = RANDOM.nextInt(options.length);
			if (count < 3) {
				count = 3;
			}
			List<String> list = new ArrayList<>(Arrays.asList(options));
			Collections.shuffle(list);
			for (int i = 0; i < count && i < options.length; i++) {
				result.append(list.get(i).trim());
				if (i != count - 1) {
					result.append(" / ");
				}
			}
		}
	}

	private static final Random RANDOM = new Random();

	/**
	 * 
	 * 
	 * @author devstudy
	 * @see http://devstudy.net
	 */
	private static class Category implements Comparable<Category> {
		final String name;
		final File imageFile;
		final List<Producer> producers;

		Category(String name, File imageFile) {
			super();
			this.name = name;
			this.imageFile = imageFile;
			this.producers = Collections.unmodifiableList(createProducers());
		}

		private List<Producer> createProducers() {
			String staticFieldName = name.toUpperCase().replace("-", "_") + "_PRODUCER";
			try {
				Field field = TestDataGenerator.class.getDeclaredField(staticFieldName);
				String[] data = (String[]) field.get(TestDataGenerator.class);
				List<Producer> result = new ArrayList<>();
				for (String producer : data) {
					result.add(new Producer(producer));
				}
				return result;
			} catch (NoSuchFieldException e) {
				System.err.println(staticFieldName + " field not found for category: " + name);
				return Collections.emptyList();
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}

		int getProductCount() {
			int res = 0;
			for (Producer p : producers) {
				res += p.productCount;
			}
			return res;
		}

		@Override
		public int compareTo(Category o) {
			return name.compareToIgnoreCase(o.name);
		}
	}

	/**
	 * 
	 * 
	 * @author devstudy
	 * @see http://devstudy.net
	 */
	private static class Producer {
		final String name;
		final int productCount;

		Producer(String name) {
			this(name, RANDOM.nextInt(35) + 5);
		}

		Producer(String name, int productCount) {
			super();
			this.name = name;
			this.productCount = productCount;
		}
	}

	/**
	 * 
	 * 
	 * @author devstudy
	 * @see http://devstudy.net
	 */
	private static class Product {
		final int idCategory;
		final String category;
		final int idProducer;
		final String name;
		final String imageLink;

		Product(String category, int idCategory, int idProducer, String name, String imageLink) {
			super();
			this.category = category;
			this.idCategory = idCategory;
			this.idProducer = idProducer;
			this.name = name;
			this.imageLink = imageLink;
		}
	}
}
