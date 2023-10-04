package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{
//        Train train =trainRepository.getOne(bookTicketEntryDto.getTrainId());
//        if(train==null) return null;
//        String route=train.getRoute();
//        String[]arr=route.split(",");
//        int indexOfDestination=-1;
//        int indexOfStarting=-1;
//        Station destination=bookTicketEntryDto.getToStation();
//        Station start=bookTicketEntryDto.getFromStation();
//        Map<String,Integer> hm=new HashMap<>();
//        for(int i=0;i<arr.length;i++){
//            hm.put(arr[i],i);
//            if(indexOfDestination==-1 && arr[i].equals(destination.toString())){
//                indexOfDestination=i;
//            }
//            if(indexOfStarting==-1 && arr[i].equals(start.toString())){
//                indexOfStarting=i;
//            }
//        }
//        if(indexOfDestination==-1 || indexOfStarting==-1 || indexOfStarting > indexOfDestination) throw new Exception("Invalid stations");
////        if(indexOfStarting==-1)return null;
//        int []pref=new int[arr.length];
//        List<Ticket>bookedticket=train.getBookedTickets();
//        for(Ticket ticket:bookedticket){
//            List<Passenger>passeng=ticket.getPassengersList();
//            pref[hm.get(ticket.getFromStation().toString())]+=passeng.size();
//            pref[hm.get(ticket.getToStation().toString())]-=passeng.size();
//        }
//        int occupiedSeat=-1;
//        for(int i=1;i<pref.length;i++){
//            pref[i]+=pref[i-1];
//        }
//        for(int i=indexOfStarting;i<pref.length && i< indexOfDestination;i++){
//            occupiedSeat=Math.max(pref[i],occupiedSeat);
//        }
//        int totalNoSeat=train.getNoOfSeats();
//         if(totalNoSeat-occupiedSeat<bookTicketEntryDto.getNoOfSeats()){
//             throw new Exception("Less tickets are available");
//         }
//         int fare=(indexOfDestination-indexOfStarting)*300;
//         List<Passenger>passengerList=new ArrayList<>();
//         for(int id:bookTicketEntryDto.getPassengerIds()){
//             passengerList.add(passengerRepository.getOne(id));
//         }
//
//         Ticket ticket=new Ticket(0,passengerList,train,start,destination,fare);
//         for(Passenger pass:passengerList){
//             List<Ticket>tic=pass.getBookedTickets();
//             tic.add(ticket);
//             pass.setBookedTickets(tic);
//         }
//         bookedticket.add(ticket);
//         train.setBookedTickets(bookedticket);
//
//         Ticket savedTicket=ticketRepository.save(ticket);
//         Train savedTrain=trainRepository.save(train);
//         return savedTicket.getTicketId();
        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

//       return null;
        int trainId = bookTicketEntryDto.getTrainId();
        Optional<Train> trainOptional = trainRepository.findById(trainId);
        if(!trainOptional.isPresent()){
            throw new Exception("Train not found");
        }
        Train train = trainOptional.get();
        List<Ticket> tickets = train.getBookedTickets();
        int seatsNeeded = bookTicketEntryDto.getNoOfSeats();
        int totalSeatsInTrain = train.getNoOfSeats();
        int availableSeatsInTrain = totalSeatsInTrain - tickets.size();
        // In case the there are insufficient tickets throw new Exception("Less tickets are available");
        if(availableSeatsInTrain < seatsNeeded){
            throw new Exception("Less tickets are available");
        }
        //In case the train doesn't pass through the requested stations throw new Exception("Invalid stations");
        Station fromStation = bookTicketEntryDto.getFromStation();
        Station toStation = bookTicketEntryDto.getToStation();
        String[] trainRoute = train.getRoute().split(",");
        int left = 0, right = trainRoute.length-1;
        boolean fromStationFound = false, toStationFound = false;
        while(left < right){
            if(trainRoute[left].equals(fromStation.toString())){
                fromStationFound = true;
                break;
            }
            left++;
        }
        while(left < right){
            if(trainRoute[right].equals(toStation.toString())){
                toStationFound = true;
                break;
            }
            right--;
        }
        if(!fromStationFound || !toStationFound){
            throw new Exception("Invalid stations");
        }
        //otherwise book the ticket, calculate the price and other details. Save the information in corresponding DB Tables
        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();
        Ticket ticket = new Ticket();
        ticket.setFromStation(fromStation);
        ticket.setToStation(toStation);
        //Fare System : Check problem statement
        int totalFairPerTicket = Math.abs(right - left) * 300;
        int totalFair = totalFairPerTicket * seatsNeeded;
        ticket.setTotalFare(totalFair);
        ticket.setTrain(train);
        Ticket savedTicket = ticketRepository.save(ticket);

        //Save the bookedTickets in the train Object
        train.getBookedTickets().add(savedTicket);

        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        Optional<Passenger> passengerOptional = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        Passenger bookingPassenger = passengerOptional.get();
        bookingPassenger.getBookedTickets().add(savedTicket);
        savedTicket.getPassengersList().add(bookingPassenger);
        for(Integer id : passengerIds){
            Optional<Passenger> passenger = passengerRepository.findById(id);
            passenger.get().getBookedTickets().add(savedTicket);
            savedTicket.getPassengersList().add(passenger.get());

        }
        ticketRepository.save(savedTicket);
        trainRepository.save(train);
        passengerRepository.save(bookingPassenger);
        //And the end return the ticketId that has come from db
        return savedTicket.getTicketId();

    }
}
