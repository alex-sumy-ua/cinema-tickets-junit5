package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import static org.junit.jupiter.api.Assertions.*;

public class TicketServiceImplMockitoTest {

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test
    void testPurchaseTicketsValidOrder() throws InvalidPurchaseException {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest ticket2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        Long accountId = 123L;

        // Act
        ticketService.purchaseTickets(accountId, ticket1, ticket2);

        // Assert
        verify(paymentService).makePayment(eq(accountId), eq(65));  // 2 adults * 25 + 1 child * 15 = 55
        verify(seatReservationService).reserveSeat(eq(accountId), eq(3));  // 2 adults + 1 child = 3 seats
    }

    @Test
    void testPurchaseTicketsNoAdult() {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        Long accountId = 123L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, ticket1);
        });
        assertEquals("Invalid order. At least one Adult ticket is required.", exception.getMessage());
    }

    @Test
    void testPurchaseTicketsExceedsMaxLimit() {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);
        Long accountId = 123L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, ticket1);
        });
        assertEquals("Invalid order. Maximum 25 tickets are allowed per purchase.", exception.getMessage());
    }

    @Test
    void testPurchaseTicketsInvalidAccount() {
        // Arrange
        TicketTypeRequest ticket1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        Long accountId = -1L;

        // Act & Assert
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, ticket1);
        });
        assertEquals("Invalid account ID. Account ID must be greater than zero.", exception.getMessage());
    }
}
