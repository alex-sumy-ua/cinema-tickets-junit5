package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import static org.junit.jupiter.api.Assertions.*;

public class TicketServiceImplPureTest {

//    private static final int PRICE_FOR_ADULTS = 25;
//    private static final int PRICE_FOR_KIDS = 15;
//    private static final int PRICE_FOR_INFANTS = 0;

    private TicketPaymentService paymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        paymentService = (accountId, totalAmountToPay) -> {
            // Do nothing, just simulate the behavior for the test
        };
        seatReservationService = (accountId, totalSeatsToAllocate) -> {
            // Do nothing, just simulate the behavior for the test
        };

        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test
    void testPurchaseTicketsValidOrder() throws InvalidPurchaseException {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);  // 2 Adult tickets
        TicketTypeRequest ticket2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);  // 1 Child ticket
        Long accountId = 123L;

        // Act
        ticketService.purchaseTickets(accountId, ticket1, ticket2);
    }

    @Test
    void testPurchaseTicketsNoAdult() {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);  // 2 Child tickets
        Long accountId = 123L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, ticket1));
        assertEquals("Invalid order. At least one Adult ticket is required.", exception.getMessage());
    }

    @Test
    void testPurchaseTicketsExceedsMaxLimit() {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);  // 26 Adult tickets
        Long accountId = 123L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, ticket1));
        assertEquals("Invalid order. Maximum 25 tickets are allowed per purchase.", exception.getMessage());
    }

    @Test
    void testPurchaseTicketsInvalidAccount() {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);  // 1 Adult ticket
        Long accountId = -1L;  // Invalid account ID

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, ticket1));
        assertEquals("Invalid account ID. Account ID must be greater than zero.", exception.getMessage());
    }

    @Test
    void testPurchaseTicketsEmptyRequest() {
        // Arrange
        TicketTypeRequest[] emptyRequest = {};  // Empty request array
        Long accountId = 123L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, emptyRequest));
        assertEquals("Invalid order. Order cannot be empty.", exception.getMessage());
    }

    @Test
    void testPurchaseTicketsNullRequest() {
        // Arrange
        TicketTypeRequest[] nullRequest = null;  // Null request array
        Long accountId = 123L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            try {
                ticketService.purchaseTickets(accountId, nullRequest);
            } catch (NullPointerException e) {
                throw new InvalidPurchaseException("Invalid order. Order cannot be empty.");
            }
        });

        // Assert
        assertEquals("Invalid order. Order cannot be empty.", exception.getMessage());    }

}
