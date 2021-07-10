package kumbhakarna.model;

/**
 * Stae Changes Possible
 *
 * AVAILABLE -> OCCUPIED
 * OCCUPIED -> CHECKOUT
 * CHECKOUT -> Available
 */
public enum  RoomStatus {
    AVAILABLE, //GREEN
    CHECKOUT,  // GREY
    OCCUPIED, // RED
    MAINTENANCE,
    ROOMFIXED
}
