package org.mtransit.parser.ca_vernon_transit_system_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://bctransit.com/*/footer/open-data
// https://bctransit.com/servlet/bctransit/data/GTFS - Vernon
public class VernonTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-vernon-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new VernonTransitSystemBusAgencyTools().start(args);
	}


	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Vernon Regional Transit System bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Vernon Regional Transit System bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "17"; // Vernon Regional Transit System only

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!INCLUDE_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 1: return "004B8E";
			case 2: return "8AC641";
			case 3: return "F68C1F";
			case 4: return "8E0C3A";
			case 5: return "E81D89";
			case 6: return "01AEF0";
			case 7: return "00AB4F";
			case 8: return "B3AB7D";
			case 11: return "FCAF18";
			case 60: return "A7439B";
			case 61: return "B3B828";
			case 90: return AGENCY_COLOR_BLUE;
			// @formatter:on
			}
			if (isGoodEnoughAccepted()) {
				return AGENCY_COLOR_BLUE;
			}
			System.out.printf("\nUnexpected route color for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(1L, new RouteTripSpec(1L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Coldstream") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"144021", // Westbound Coldstream Creek at McClounie
								"144027", // ++
								"144275", // Downtown Exchange Bay D
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"144275", // Downtown Exchange Bay D
								"144012", // ++
								"144021", // Westbound Coldstream Creek at McClounie
						})) //
				.compileBothTripSort());
		map2.put(2L, new RouteTripSpec(2L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Pleasant Valley", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"144000", // Downtown Exchange Bay A
								"144049", // ++
								"144061", // Northbound Pleasant Valley at Silver Star
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"144061", // Northbound Pleasant Valley at Silver Star
								"144068", // ++
								"144000", // Downtown Exchange Bay A
						})) //
				.compileBothTripSort());
		map2.put(3L, new RouteTripSpec(3L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walmart", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"144276", // Downtown Exchange Bay F
								"144084", // ++
								"144094", // Eastbound 58 Ave at 20 St #Walmart
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"144094", // Eastbound 58 Ave at 20 St #Walmart
								"144103", // ++
								"144276", // Downtown Exchange Bay F
						})) //
				.compileBothTripSort());
		map2.put(4L, new RouteTripSpec(4L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lakeview Pk", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"144275", // Downtown Exchange Bay D
								"144116", // ++
								"144123", // Southbound 18 St at 30 Ave
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"144123", // Southbound 18 St at 30 Ave
								"144131", // ++
								"144275", // Downtown Exchange Bay D
						})) //
				.compileBothTripSort());
		map2.put(5L, new RouteTripSpec(5L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "South Vernon") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"144147", // Northbound Okanagan at S Vernon
								"144158", // ++
								"144276", // Downtown Exchange Bay F
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"144276", // Downtown Exchange Bay F
								"144140", // ++
								"144147", // Northbound Okanagan at S Vernon
						})) //
				.compileBothTripSort());
		map2.put(6L, new RouteTripSpec(6L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "College") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"144170", // Northbound 9330 block Hwy 97
								"144175", // ++
								"144037", // Downtown Exchange Bay C
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"144037", // Downtown Exchange Bay C
								"144285", // ++
								"144170", // Northbound 9330 block Hwy 97
						})) //
				.compileBothTripSort());
		map2.put(7L, new RouteTripSpec(7L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lakeshore") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"144207", // Westbound Lakeshore at Tronson
								"144216", // ++
								"144111", // ==
								"144038", // Downtown Exchange Bay B
								"144000", // Downtown Exchange Bay A
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"144000", // Downtown Exchange Bay A
								"144038", // Downtown Exchange Bay B
								"144178", // ==
								"144184", // ==
								"144186", // == xx
								"144187", // ==
								"144189", // ==
								//
								"144190", // !=
								"144197", // !=
								//
								"144310", // != xx
								"144311", // != xx
								"144312", // !=
								"144313", // !=
								"144250", // !=
								"144310", // != xx
								"144311", // != xx
								//
								"144186", // == xx
								"144199", // ==
								"144207", // Westbound Lakeshore at Tronson
						})) //
				.compileBothTripSort());
		map2.put(8L, new RouteTripSpec(8L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bella Vista") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"144257", // Northbound Tronson at Bella Vista
								"144215", // ++
								"144038", // Downtown Exchange Bay B
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"144038", // Downtown Exchange Bay B
								"144251", // ++
								"144257", // Northbound Tronson at Bella Vista
						})) //
				.compileBothTripSort());
		map2.put(60L, new RouteTripSpec(60L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Enderby", // Amstrong
				1, MTrip.HEADSIGN_TYPE_STRING, "Vernon") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"144274", // Downtown Exchange Bay E
								"144045", // ==
								"106017", // !=
								"144084", // ==
								"544004", // ==
								"144229", // Northbound 3200 block Smith
								"144294", // Eastbound Mill at George
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"144294", // Eastbound Mill at George
								"144292", // Southbound Smith at Pleasant Valley
								"144274", // Downtown Exchange Bay E
						})) //
				.compileBothTripSort());
		map2.put(61L, new RouteTripSpec(61L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Lumby", // Lavington
				1, MTrip.HEADSIGN_TYPE_STRING, "Vernon") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"144000", // Downtown Exchange Bay A
								"544010", // == Eastbound Hwy 6 at Freeman
								"544008", // !=
								"144246", // ==
								"144266", // Southbound Norris at Glencaird
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"144266", // Southbound Norris at Glencaird
								"144281", // ==
								"544007", // !=
								"544006", // Westbound Hwy 6 at Freeman
								"144000", // Downtown Exchange Bay A
						})) //
				.compileBothTripSort());
		map2.put(90L, new RouteTripSpec(90L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Vernon", //
				1, MTrip.HEADSIGN_TYPE_STRING, "UBCO") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"140104", // Northbound Alumni Ave at Transit Way Bay E
								"103654", // ++
								"144274", // Downtown Exchange Bay E
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"144274", // Downtown Exchange Bay E
								"144265", // ++
								"140104", // Northbound Alumni Ave at Transit Way Bay E
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	private static final String EXCH = "Exch";
	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W){1}(exchange)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCH + "$4";

	private static final Pattern CLEAN_P1 = Pattern.compile("[\\s]*\\([\\s]*");
	private static final String CLEAN_P1_REPLACEMENT = " (";
	private static final Pattern CLEAN_P2 = Pattern.compile("[\\s]*\\)[\\s]*");
	private static final String CLEAN_P2_REPLACEMENT = ") ";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CLEAN_P1.matcher(tripHeadsign).replaceAll(CLEAN_P1_REPLACEMENT);
		tripHeadsign = CLEAN_P2.matcher(tripHeadsign).replaceAll(CLEAN_P2_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		System.out.printf("\nUnexpected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
