package myairticket

import groovy.cli.picocli.CliBuilder
import java.time.LocalDate
import io.github.bonigarcia.wdm.WebDriverManager
import geb.Browser
import geb.Page

import myairticket.qunar.QuNarHomePage

class Main{

    static void main (String... args){
        def cli = new CliBuilder(usage:'myairticket [options]', header: 'Options:')
        cli.h("print this message")
        cli.f(longOpt:'from', args:1, argName: 'airport code', 'Departure city, for example, YYZ for Toronto Pearson Airport.')
        cli.t(longOpt:'to', args:1, argName: 'airport code', 'Destination city, for example, PEK for Beijing Airport. (Optional)')
        cli.d(longOpt:'departure', args:1, argName: 'departure date', 'Date in YYYY/MM/DD (2020/06/16)')
        cli.r(longOpt:'return', args:1, argName: 'return date', 'Date in YYYY/MM/DD (2020/08/18). (Optional)')
        cli.v(longOpt:'verbose', defaultValue:false, "show debug information.")

        cli.e(longOpt:'env', args:1, argName:'environment', defaultValue: 'test', 
            'Banner environment to read data from, default is TEST environment')
        cli.o(args:1, argName:'outputFolder', defaultValue: './out/', 'output path for csv files, default is ./out/')
        cli.u(args:1, argName:'username:password', 'Banner database username and password')
        cli.l(defaultValue:false, "list all modules and files to create")
        cli.m(args:'+', optionalArg: true, valueSeparator:',', argName:'module', 'modules to process, default is all')
//        cli.t(args:1, argName:'termcode', defaultValue: LocalDate.now().getYear().toString()+'01', 
//            'term code, such as 201901, default is current_year+01')
//        cli.f(args:'+', optionalArg: true, valueSeparator:',', argName:'faculties', 
//            'facuties to add, default is TE, HP, BC, CH, XX, AC and AD.')
        
        def options = cli.parse(args)
        if(options.h){
            cli.usage()
            return
        }
        def params = [:]
/*
        if(options.arguments().size()>0){
            def command = new ProgramsImport(
                                serverUrl:'http://gbcdcu01u.gbcuat.local/InfosilemAcademicSuiteAPI/TimetablerImportExportServices.svc',
                                serverName: 'sqlsj-uat',
                                dbName: 'TEST222')
            println command.test()
            //command.results.each{
            //    println it
            //}
            println command.resultCode
            println command.resultErrorDetail
            return
        }
*/
        if (options.f && options.f.size()!=3){
            println "Invalid airport code: ${options.f}"
            return
        }
        if (options.t && options.t.size()!=3){
            println "Invalid airport code: ${options.t}"
            return
        }
        
        params.fromAirport = options.f
        params.toAirport = options.t
        params.departureDate = options.d
        params.returnDate = options.r
        params.verbose = options.v
        
        if(options.u){
            def sa = options.u.split(':')
            params.dbUser = sa[0]
            if(sa.size()>1) params.dbPassword = sa[1]
        }
        params.outputFolder = options.o
        params.list = options.l
//        params.faculties = ['TE', 'HP', 'BC', 'CH', 'XX', 'AC', 'AD'] + (ArrayList)(options.fs?:[])
        def modules = options.ms

        println "From Airport:          ${params.fromAirport}"
        println "To Airport:            ${params.toAirport}"
//        println "Term code:             ${params.termCode}"
        println "Departure Date:        ${params.departureDate}"
        println "Return Date:           ${params.returnDate}"
        println "Verbose:               ${params.verbose}"

        WebDriverManager.chromedriver().setup()
        Browser.drive {
            to QuNarHomePage
            searchFlights(params)
            at QunarFlightsPage
            if(!hasResult()){
                println "no flights found from ${params.fromAirport} to ${params.toAirport} in ${params.departureDate}."
            }

        }
        
        System.in.read();

        return
    }
}