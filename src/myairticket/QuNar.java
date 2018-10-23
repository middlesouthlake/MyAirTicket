
package myairticket;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class QuNar{
	static String linkHome = "https://m.flight.qunar.com/h5/flight/?bd_source=qunar";
	
	//used in main page
	static final String strRoundTrip = "往返";
	static final String strDeparture = "出发";
	static final String strDepartureDate = "去程日期";
	static final String strReturnDate = "返程日期";
	static final String strArrival = "到达";
	
	//used in date setting page
	static final String strGoTab = "go-tab";
	static final String strBackTab = "back-tab";
	
	
	WebDriver driver;
	ArrayList<QuNarFlight> flights;
	
	boolean isRoundTrip = true;
	String codeDeparture="YYZ";
	String codeArrival="PEK";
	GregorianCalendar departureDate;
	GregorianCalendar returnDate;
	int daysDepartureRange = 7;
	int daysReturnRange = 7;
	
	
	
	QuNar(String link) {
	    FirefoxOptions options = new FirefoxOptions();
	    options.setLogLevel(FirefoxDriverLogLevel.FATAL);
	    //options.setHeadless(true);
	    // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
        //driver = new FirefoxDriver(options);
	    
	    ChromeOptions chromeOptions = new ChromeOptions();  
	    chromeOptions.addArguments("--headless");  
	    
	    System.setProperty("webdriver.chrome.driver", "/home/westmount/selenium-java-3.8.1/chromedriver");
	    driver = new ChromeDriver(chromeOptions);
        flights = new ArrayList<QuNarFlight>();

        // And now use this to visit Google
        driver.get(link);
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");		
	}
	public void closeDriver() {
		driver.quit();
	}
	public WebDriver getDriver() {
		return driver;
	}
	public static GregorianCalendar string2Calendar(String date, String defaultDate) {
		String s;
		
		if(date.matches("2\\d\\d\\d[/-][01]\\d[/-][0123]\\d$"))
			s = date;
		else if (defaultDate.matches("2\\d\\d\\d[/-][01]\\d[/-][0123]\\d$"))
			s = defaultDate;
		else
			return null;
		
		String[] ss = s.split("[/-]");
		return new GregorianCalendar(Integer.parseInt(ss[0]), Integer.parseInt(ss[1])-1, Integer.parseInt(ss[2]));
	}
	public void loadProperties(String filename) {
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream(filename);
			prop.load(input);
			isRoundTrip = prop.getProperty("RoundTrip", "true").toLowerCase().equals("true");
			codeDeparture = prop.getProperty("DepartureAirport","YYZ");
			codeArrival = prop.getProperty("ArrivalAirport", "PEK");
			String date = prop.getProperty("DepartureDate", "2018/07/02");
			departureDate = string2Calendar(date, "2018/07/02");
			date = prop.getProperty("ReturnDate", "2018/08/23");
			returnDate = string2Calendar(date, "2018/08/23");

			String strDays = prop.getProperty("DepartureDateRange", "3");
			if(strDays.matches("\\d+$")) {
				daysDepartureRange = Integer.parseInt(strDays);
			}
			else
				daysDepartureRange = 3;
		
			strDays = prop.getProperty("ReturnDateRange", "3");
			if(strDays.matches("\\d+$")) {
				daysReturnRange = Integer.parseInt(strDays);
			}
			else
				daysReturnRange = 3;
		}catch(IOException ex) {
			System.out.println("File I/O error. Load default properties.");;
			isRoundTrip = true;
			codeDeparture="YYZ";
			codeArrival="PEK";
			departureDate = new GregorianCalendar(2018, 7-1, 2);
			daysDepartureRange = 7;
			returnDate = new GregorianCalendar(2018, 8-1, 23);
			daysReturnRange = 7;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	


	public static String getStringDate(Calendar date) {
		return String.format("%4d/%02d/%02d", date.get(Calendar.YEAR), 
				date.get(Calendar.MONTH)+1, date.get(Calendar.DAY_OF_MONTH));
	}
	
	public static void setRoundTrip(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        
        //1. change the "single trip" or "round trip"
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.className("tabItemList")));
        
        // Find the text input element by its name
        //WebElement element = driver.findElement(By.className("tabItemList"));
                      
        List<WebElement> allOptions = element.findElements(By.className("tabItem"));
        
        for (WebElement option : allOptions) {
            //System.out.println(String.format("Value is: %s", option.getAttribute("value")));
           //System.out.println(option.getText());
           if(option.getText().equals(strRoundTrip)) {
        	   option.click();
           }
        }
	}
	public static void setCity(WebDriver driver, String airportCode) {
    	WebElement input = driver.findElement(By.className("search-input"));
    	input.sendKeys(airportCode);
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        WebDriverWait wait = new WebDriverWait(driver, 30);
        WebElement result = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("search-dialog")));
        List<WebElement> cities = result.findElements(By.xpath("//div[@class=' line']"));
        for(WebElement city:cities) {
        	//System.out.println(city.getText());
        	if(city.getText().contains(airportCode)==true) {
        		wait.until(ExpectedConditions.elementToBeClickable(city));
        		city.click();
        		break;
        	}
        }
	}
		
	public static void setDate(WebDriver driver, String typeTrip, String strDate) {
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.elementToBeClickable(By.className(strGoTab)));
        //result.click();
		
        try {
        	WebElement day = driver.findElement(By.xpath("//div[@data-date='"+strDate+"']"));
            String s = day.getText();
            day.click();
            day = driver.findElement(By.xpath("//div[@data-date='"+strDate+"']"));
            if(day.getText().equals(s)==true) day.click();
        }catch(NoSuchElementException e) {
        	System.out.println("Not found");
        }		
	}
	
	
    public static void main(String[] args) {

    	QuNar quNar = new QuNar(linkHome);
    	
    	String confName = (args.length==1)? args[0]:"myairticket.conf";
    	quNar.loadProperties(confName);
    	//System.out.println(getStringDate(quNar.departureDate));
    	
    	WebDriver driver = quNar.getDriver();

    	setRoundTrip(driver);
    	

        //2. set departure city
    	List<WebElement> allOptions = driver.findElements(By.className("searchFlight"));
        
        for (WebElement option: allOptions) {
        	//System.out.println(option.getText());
        	if(option.findElement(By.className("label-l")).getText().equals(strDeparture)) {
        		option.findElement(By.className("select-l")).click();
        		setCity(driver, quNar.codeDeparture);
        		System.out.println("Departure City: "+quNar.codeDeparture);
        	}
        }
        
        //3.set arrival city
    	allOptions = driver.findElements(By.className("searchFlight"));
        
        for (WebElement option: allOptions) {
        	//System.out.println(option.getText());
        	if(option.findElement(By.className("label-r")).getText().equals(strArrival)) {
        		option.findElement(By.className("select-r")).click();
        		setCity(driver, quNar.codeArrival);
        		System.out.println("Departure City: "+quNar.codeArrival);
        	}
        }
        
        GregorianCalendar dc = (GregorianCalendar) quNar.departureDate.clone();
        GregorianCalendar rc = (GregorianCalendar) quNar.returnDate.clone();
        for(int d=0;d<quNar.daysDepartureRange;d++) {
        	for(int r=0;r<quNar.daysReturnRange;r++) {
		        //4. set departure date and return date
        		try {
        			allOptions = driver.findElements(By.className("searchFlight")); 
        		}
        		catch(Exception e) {
        			System.out.println("Error on: allOptions = driver.findElements(By.className(\"searchFlight\"));");
        			continue;
        		}
		        for (WebElement option: allOptions) {
		        	//System.out.println(option.getText());
		        	if(option.findElement(By.className("label-l")).getText().equals(strDepartureDate)) {
		        		option.findElement(By.className("select-l")).click();
		                setDate(driver, strGoTab, getStringDate(dc));
		                System.out.print("Departure Date:"+getStringDate(dc));
				        try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
		                option.findElement(By.className("select-r")).click();
		                setDate(driver, strGoTab, getStringDate(rc));
		                System.out.println(",Return Date:"+getStringDate(rc));
				        try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
		        	}
		        }
		        
		        //5. click the search button
		        try {
		        	WebElement searchButton = driver.findElement(By.className("searchBtn"));
			        searchButton.click();
		        }catch(Exception e) {
		        	continue;
		        }
		        
		        //6. enter the search result page, filter to direct flights first
		        WebDriverWait wait;
		        for(int i=0;i<5;i++)
		        	try {
		        		wait = new WebDriverWait(driver, 30);
		        		WebElement directButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[@id='interDirectItem']")));
		        		wait.until(ExpectedConditions.elementToBeClickable(directButton));
		        		directButton.click();
		        		break;
		        	}catch(Exception e) {
		        		driver.navigate().refresh();
		        		System.out.println("Load page error.xpath(\"//li[@id='interDirectItem']\")");
				        try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
		        	}
		        
		        //7. get the list of the flights        
		        try {
					Thread.sleep(15000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		
		    	/*style tag format
		    	 * @font-face {font-family: 'ddv1c4021eb338srt';
		        src: url('//s.qunarzz.com/flight_touch/ddvfonts/20180117020000/ddv1c4021eb338srt.ttf') format('truetype');
		    	}
		    	.ddv1c4021eb338srt{
		        	font-family: 'ddv1c4021eb338srt'!important;
		    	}*/
		        try {
			        List<WebElement> fontFiles = driver.findElements(By.xpath("//style"));
					for(WebElement font:fontFiles) {
						String s = font.getAttribute("innerHTML");
						if(s==null) {
							System.out.println("get innerHTML error.");
							continue;
						}
						String[] ss = s.split("'");
				        QuNarFlight.buildFontLibrary(ss[1], "http:"+ss[3]);
					}
			
			        List<WebElement> listFlights = driver.findElements(By.xpath("//li[@class='list-row item']"));
			        List<WebElement> listFonts = driver.findElements(By.className("price1"));
			        List<WebElement> listPrices = driver.findElements(By.xpath("//span[@class='price']"));
	
			        WebElement flight, priceFont, priceInfo;
			        for(int i=0;i<listFlights.size();i++) {
			        	if(i>=listFonts.size() || i>=listPrices.size()) {
			        		System.out.println("list size error.");
			        		break;
			        	}
			        	flight = listFlights.get(i);
			        	priceFont = listFonts.get(i);
			        	priceInfo = listPrices.get(i);
			        	
			        	QuNarFlight f = new QuNarFlight();
			
			        	String strFlightCode = flight.getAttribute("data-flight-code");
			        	if(strFlightCode==null) {
			        		System.out.println("flight code is null.");
			        		continue;
			        	}
			        	f.setFlightCode(strFlightCode);
			        	f.setListPrice(priceInfo.getText());
			        	String s = priceFont.getAttribute("class");
			        	if(s==null) {
			        		System.out.println("font name is null.");
			        		continue;
			        	}
			        	f.setPriceFontName(s.split(" ")[2]);
			        	quNar.flights.add(f);
			        }
		        }catch(Exception e) {
		        	continue;
		        }
		        try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		        wait = new WebDriverWait(driver, 30);
		        WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("goBack")));
		        backButton.click();
		        //driver.findElement(By.id("goBack")).click();
		        rc.add(Calendar.DAY_OF_MONTH, 1);
        	}
        	dc.add(Calendar.DAY_OF_MONTH, 1);
            rc = (GregorianCalendar) quNar.returnDate.clone();
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        }            
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        
        driver.quit();

        for(QuNarFlight f : quNar.flights) {
        	f.decryptPrice();
        }
        Collections.sort(quNar.flights);
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println("---------------------------------------------");
        System.out.println(dateFormat.format(cal.getTime()));
    	System.out.println("Flights list according price from low to high:");
        for(QuNarFlight f : quNar.flights) {
        	System.out.format("%5d %s\n", f.getPrice(), f.getFlightCode());
        }
        
        System.exit(0);
        
    }
}