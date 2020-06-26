packate myairticket.qunar
import geb.Page
import geb.waiting.WaitTimeoutException

class QunarFlightsPage extends Page {
    static at = { title == "机票预订，机票查询" }
    boolean verbose = false
    static content = {
        flightListBlock { $('#flightList') }
        directFlightIcon { $('i.icon.i-direct_flight')}
    }

    def setDirectFlight(){
        directFlightIcon.click()
    }

    boolean hasResult(){
        try{
            waitFor (30) { $('i.icon.i-direct_flight') }
            println flightListBlock.text()
            //!flightListBlock.text().contains('没有查询到符合条件的航班')
            def fonts = $('style');
            println "fonts size is ${fonts.size()}."
            fonts.each{
                println it.innerHTML
            }
        }catch (WaitTimeoutException e){
            return false
        }
        return true
    }
}