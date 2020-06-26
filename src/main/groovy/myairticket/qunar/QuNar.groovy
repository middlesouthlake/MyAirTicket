package myairticket.qunar

import geb.Browser
import geb.Module
import geb.Page
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.Keys

class QuNarHomePage extends Page{
    static url = "https://m.flight.qunar.com/h5/flight/?bd_source=qunar"
    static at = { title == "机票预订，机票查询" }
    boolean verbose = false
    static content = {
        tabItems { $("li.tabItem") }
        roundTripTab {$("li.tabItem", text:contains("往返"))}

        fromCitySelector { $("div.select-l", 0) }
        toCitySelector { $("div.select-r", 0) }
        departureDateSelector {$("div.select-l", 1)} //class:'roundGoTime')}
        returnDateSelector { $("div.select-r", 1) }
        searchButton { $("button.searchBtn") }

    }
    void setTrip(String trip='round'){
        if(trip.equalsIgnoreCase('round')){
            setRoundTrip()
        } else if (trip.equalsIgnoreCase('international')){
            setInternationalTrip()
        } else {
            setDemesticTrip()ß
        }

    }
    void setDemesticTrip(){

    }
    void setInternationalTrip(){

    }
    
    void setRoundTrip(){
        roundTripTab.click()
        waitFor { roundTripTab.hasClass("on") }
    }

    void setCity(String airportCode){
        waitFor {$("input.search-input")}
        $("input.search-input") << airportCode
        waitFor {$("div.search-dialog")}
        if($("div.noSearch")){
            throw new Exception("Invalid airport code.")
        }
        //Thread.sleep(2000);
        //println $("div.search-dialog").children().length
        if($("div.line", text:iContains(airportCode)).size()>1){
            //throw new Exception("Multiple entries for an airport code.")
            //$("div.line", text:iContains(airportCode), 0).click()
            if(verbose) println "multiple entries for airport code: ${airportCode}."
        }
        $("div.line", 0, text:iContains(airportCode)).click()
        
        //println $("div.search-dialog").length()
        
    }
    void setDepartureCity(String airportCode){
        fromCitySelector.click()
        if (verbose) println "airportCode:${airportCode}"
        setCity(airportCode)
        //$("input.search-input") << Keys.ENTER
    }
    void setArrivalCity(String airportCode){
        toCitySelector.click()
        if (verbose) println "airportCode:${airportCode}"
        setCity(airportCode)
    }

    void setDates(String departureDate, String returnDate=null){
        //Thread.sleep(2000);
        if(!departureDate)
            throw new Exception("Invalid departure date.")
        departureDateSelector.click()
        waitFor {$("div.go-tab")}
        $("div[data-date*='${departureDate}']").click()
        Thread.sleep(1000);

        if(returnDate)
            $("div[data-date*='${returnDate}']").click()
        //$(By.xpath("//div[@data-date='${date}']")).click()
    }

    void searchFlights(def params=[:]){
        if(params.returnDate) setRoundTrip()
        setDepartureCity(params.fromAirport)
        Thread.sleep(1000);
        setArrivalCity(params.toAirport)
        Thread.sleep(1000);
        setDates(params.departureDate, params.returnDate)
        Thread.sleep(1000);

        searchButton.click()

    }
    def test(){
        //System.setProperty("webdriver.chrome.driver", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
        //driver = { new ChromeDriver() }
        
        //WebDriver driver = new ChromeDriver();
        //driver.get("http://www.google.com/");
        //System.out.println(driver.getTitle());
        //driver.quit();
        //browser = new Browser(driver: new ChromeDriver())
        //browser.go 'https://gebish.org'
        
        Browser.drive {
            /*to GebHomePage
            
            manualsMenu.open()

            manualsMenu.links[0].click()

            at TheBookOfGebPage */
            go "https://m.flight.qunar.com/h5/flight/?bd_source=qunar"

            assert title == "机票预订，机票查询" 

            $("li.tabItem", text:contains("往返")).click()

            waitFor { $("li.tabItem", text:contains("往返")).hasClass("on") }


            //$("div.menu a.manuals").click() 
            //waitFor { !$("#manuals-menu").hasClass("animating") } 

            //$("#manuals-menu a")[0].click() 

            //assert title.startsWith("The Book Of Geb") 

        }

    }
}
