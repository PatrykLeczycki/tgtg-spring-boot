package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.dto.LocationDto;
import com.pleczycki.tgtg.model.Address;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.repository.AddressRepository;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.utils.CustomModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LocationServiceTest {

    private static LocationDto locationDto;

    @InjectMocks
    private LocationService locationService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomModelMapper mapper;



    @BeforeEach
    void setup() throws ParseException {
        MockitoAnnotations.initMocks(this);
        location = initializeLocation();
        locationDto = initializeLocationDto();
        mappedLocation = getLocationMappedFromDto(locationDto);
        allLocations = getLocationList();
    }

    private static Location location;
    private static Location mappedLocation;
    private static List<Location> allLocations;

    private static List<Location> getLocationList() throws ParseException {
        List<Location> locations = new ArrayList<>();
        String dateInString = "10-01-2020";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = formatter.parse(dateInString);
        for (int i = 1; i <= 10; i++) {

            final Location location = new Location();
            Address address = new Address();

            address.setId((long) i);
            address.setStreet("Ulica" + i);
            address.setBuildingNo("" + i);
            address.setCity("Miasto" + (i % 3));
            address.setLatitude(52.165657);
            address.setLongitude(22.283413);

            location.setId((long) i);
            location.setAddress(address);
            location.setName("Nazwa" + i);
            location.setRating((i % 5) + 1);
            location.setCreatedAt(addDays(date, i));
            location.setModifiedAt(addDays(date, i));

            locations.add(location);
        }
        return locations;
    }

    private static Location initializeLocation() {
        final Location location = new Location();

        Address address = new Address();
        address.setId(1L);
        address.setStreet("Floriańska");
        address.setBuildingNo("21a");
        address.setCity("Siedlce");
        address.setLatitude(52.165657);
        address.setLongitude(22.283413);

        location.setId(1L);
        location.setName("DomDto");
        location.setAddress(address);
        location.setRating(5.0);
        location.setCreatedAt(new Date());
        location.setModifiedAt(null);

        return location;
    }

    private Location getLocationMappedFromDto(LocationDto locationDto) {
        final Location location = new Location();
        Address address = new Address();

        address.setId(locationDto.getAddress().getId());
        address.setBuildingNo(locationDto.getAddress().getBuildingNo());
        address.setStreet(locationDto.getAddress().getStreet());
        address.setCity(locationDto.getAddress().getCity());
        address.setLatitude(locationDto.getAddress().getLatitude());
        address.setLongitude(locationDto.getAddress().getLongitude());

        location.setId(4L);
        location.setName(locationDto.getName());
        location.setAddress(address);
        location.setModifiedAt(new Date());

        return location;
    }

    private static LocationDto initializeLocationDto() {
        final LocationDto locationDto = new LocationDto();

        Address address = new Address();
        address.setId(1L);
        address.setStreet("Floriańska");
        address.setBuildingNo("21a");
        address.setCity("Siedlce");
        address.setLatitude(52.165657);
        address.setLongitude(22.283413);

        locationDto.setName("DomDto");
        locationDto.setAddress(address);
        locationDto.setRating(5.0);

        return locationDto;
    }

    @Test
    void shouldAddLocation() {
        //given
        final LocationDto locationDto = new LocationDto();

        Address address = new Address();
        address.setId(1L);
        address.setStreet("Floriańska");
        address.setBuildingNo("21a");
        address.setCity("Siedlce");
        address.setLatitude(52.165657);
        address.setLongitude(22.283413);

        locationDto.setName("DomDto");
        locationDto.setAddress(address);
        locationDto.setRating(5.0);

        when(addressRepository.save(any())).thenReturn(locationDto.getAddress());
        when(mapper.map(locationDto, Location.class)).thenReturn(mappedLocation);
        when(locationRepository.save(any())).thenReturn(location);

        //when
        ResponseEntity<Location> responseEntity = locationService.addLocation(locationDto);

        //then
        assertAll(() -> {
            assertNotNull(responseEntity);
            assertThat(responseEntity.getBody().getId(), is(1L));
            assertThat(responseEntity.getBody().getName(), is(locationDto.getName()));
            assertThat(responseEntity.getBody().getRating(), is(locationDto.getRating()));
            assertThat(responseEntity.getBody().getAddress(), is(locationDto.getAddress()));
        });
    }

    @Test
    void shouldReturnAllReviews() {
        //given
        when(locationRepository.findAll()).thenReturn(allLocations);

        //when
        ResponseEntity<List<Location>> responseEntity = locationService.getAll();

        //then
        assertAll(() -> {
            assertThat(responseEntity.getBody().size(), is(allLocations.size()));
            assertThat(responseEntity.getBody().get(0), is(allLocations.get(0)));
        });
    }

    @Test
    void shouldFindLocationById() {
        //given
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));

        //when
        ResponseEntity<Location> locationResponseEntity = locationService.getLocation(1L);

        //then
        assertThat(locationResponseEntity.getBody(), is(location));
    }

    @Test
    void shouldThrowValidExceptionsWhenProcessingLocationsWithNotExistingId() {
        //given
        when(locationRepository.findById(any())).thenReturn(Optional.empty());

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> locationService.getLocation(1L));
            assertEquals("Location not found", resourceNotFoundException.getMessage());
            resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> locationService
                            .updateLocation(locationDto, 1L));
            assertEquals("Location not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void shouldThrowValidExceptionWhenProcessingLocationWithNotExistingAddress() {
        //given
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
        when(addressRepository.findById(any())).thenReturn(Optional.empty());

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> locationService
                            .updateLocation(locationDto, 1L));
            assertEquals("Address not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void shouldUpdateLocation() {
        //given
        when(locationRepository.findById(4L)).thenReturn(Optional.of(new Location(allLocations.get(3))));
        when(addressRepository.findById(any())).thenReturn(Optional.of(location.getAddress()));
        when(locationRepository.save(any())).thenReturn(mappedLocation);

        //when
        ResponseEntity<Location> locationResponseEntity = locationService.updateLocation(locationDto, 4L);

        //then
        assertAll(() -> {
            assertThat(locationResponseEntity.getBody().getId(), is(allLocations.get(3).getId()));
            assertThat(locationResponseEntity.getBody(), equalTo(mappedLocation));
            assertThat(locationResponseEntity.getBody(), not(sameInstance(allLocations.get(3))));
            assertThat(locationResponseEntity.getBody().getName(), not(allLocations.get(3).getName()));
            assertThat(locationResponseEntity.getBody().getName(), is(locationDto.getName()));
            assertThat(locationResponseEntity.getBody().getAddress(), equalTo(locationDto.getAddress()));
        });
    }

    @Test
    void shouldGetAllByCity() {
        //given
        when(locationRepository.findAll()).thenReturn(allLocations);

        //when
        ResponseEntity<List<Location>> locations = locationService.getAllByCity("Miasto2");

        //then
        assertThat(locations.getBody().size(), is(3));
        assertThat(locations.getBody(), contains(allLocations.get(1), allLocations.get(4), allLocations.get(7)));
    }
}