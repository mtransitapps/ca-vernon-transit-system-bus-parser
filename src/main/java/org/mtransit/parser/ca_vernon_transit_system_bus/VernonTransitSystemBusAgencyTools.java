package org.mtransit.parser.ca_vernon_transit_system_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.StringUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

// https://www.bctransit.com/open-data
// https://www.bctransit.com/data/gtfs/vernon.zip
public class VernonTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-vernon-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new VernonTransitSystemBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Vernon Regional Transit System bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Vernon Regional Transit System bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "17"; // Vernon Regional Transit System only

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		//noinspection deprecation
		if (!INCLUDE_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
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
			case 9: return "E170AA";
			case 11: return "FCAF18";
			case 60: return "A7439B";
			case 61: return "B3B828";
			case 90: return AGENCY_COLOR_BLUE;
			// @formatter:on
			}
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
		}
		return super.getRouteColor(gRoute);
	}

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(1L, new RouteTripSpec(1L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Coldstream") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"144021", // Westbound Coldstream Creek at McClounie
								"144027", // ++
								"144030", // Kalamalka Lake at Kalamalka
								"144275" // Downtown Exchange Bay D
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"144275", // Downtown Exchange Bay D
								"144012", // ++
								"144021" // Westbound Coldstream Creek at McClounie
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(2L, new RouteTripSpec(2L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Pleasant Valley", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"144000", // Downtown Exchange Bay A
								"144049", // ++
								"144060" // Pleasant Valley at 47 Ave =>
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"144060", // == Pleasant Valley at 47 Ave <=
								"144061", // != Pleasant Valley at Silver Star
								"144323", // != 48 Ave at Pleasant Valley Westbound !=
								"144169", // == 20 St farside 50 Ave
								"144061", // Northbound Pleasant Valley at Silver Star
								"144068", // ++
								"144000" // Downtown Exchange Bay A
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(3L, new RouteTripSpec(3L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Walmart", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"144276", // Downtown Exchange Bay F
								"144084", // ++
								"144087", // ==
								"144319", // !=
								"144088", // !=
								"144089", // ==
								"144094" // Eastbound 58 Ave at 20 St #Walmart
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"144094", // Eastbound 58 Ave at 20 St #Walmart
								"144103", // ++
								"144276" // Downtown Exchange Bay F
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(4L, new RouteTripSpec(4L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lakeview Pk", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"144275", // Downtown Exchange Bay D
								"144116", // ++
								"144123" // Southbound 18 St at 30 Ave
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"144123", // Southbound 18 St at 30 Ave
								"144131", // ++
								"144275" // Downtown Exchange Bay D
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(5L, new RouteTripSpec(5L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "South Vernon") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"144147", // Northbound Okanagan at S Vernon
								"144158", // ++
								"144276" // Downtown Exchange Bay F
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"144276", // Downtown Exchange Bay F
								"144140", // ++
								"144147" // Northbound Okanagan at S Vernon
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(6L, new RouteTripSpec(6L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "College") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"144170", // Northbound 9330 block Hwy 97
								"144175", // ++
								"144037" // Downtown Exchange Bay C
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"144037", // Downtown Exchange Bay C
								"144285", // ++
								"144170" // Northbound 9330 block Hwy 97
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(7L, new RouteTripSpec(7L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lakeshore") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"144207", // Westbound Lakeshore at Tronson
								"144216", // ++
								"144111", // ==
								"144038", // Downtown Exchange Bay B
								"144000" // Downtown Exchange Bay A
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
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
								"144207" // Westbound Lakeshore at Tronson
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(8L, new RouteTripSpec(8L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Downtown Vernon", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bella Vista") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"144257", // Northbound Tronson at Bella Vista
								"144215", // ++
								"144038" // Downtown Exchange Bay B
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"144038", // Downtown Exchange Bay B
								"144251", // ++
								"144257" // Northbound Tronson at Bella Vista
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(61L, new RouteTripSpec(61L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Lumby", // Lavington
				1, MTrip.HEADSIGN_TYPE_STRING, "Vernon") //
				.addTripSort(0, //
						Arrays.asList( //
								"144000", // Downtown Exchange Bay A
								"544010", // == Eastbound Hwy 6 at Freeman
								"544008", // !=
								"144246", // ==
								"144266" // Southbound Norris at Glencaird
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"144266", // Southbound Norris at Glencaird
								"144281", // ==
								"544007", // !=
								"544006", // Westbound Hwy 6 at Freeman
								"144000" // Downtown Exchange Bay A
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(90L, new RouteTripSpec(90L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Vernon", //
				1, MTrip.HEADSIGN_TYPE_STRING, "UBCO") //
				.addTripSort(0, //
						Arrays.asList( //
								"140104", // Northbound Alumni Ave at Transit Way Bay E
								"103654", // ++
								"144274" // Downtown Exchange Bay E
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"144274", // Downtown Exchange Bay E
								"144265", // ++
								"140104" // Northbound Alumni Ave at Transit Way Bay E
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	private static final Pattern STARTS_WITH_DASH_ = Pattern.compile("^.+- ", Pattern.CASE_INSENSITIVE);
	private static final String STARTS_WITH_DASH_REPLACEMENT = StringUtils.EMPTY;

	private static final Pattern ENDS_WITH_DASH_ = Pattern.compile("-$", Pattern.CASE_INSENSITIVE);
	private static final String ENDS_WITH_DASH_REPLACEMENT = StringUtils.EMPTY;

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = STARTS_WITH_DASH_.matcher(tripHeadsign).replaceAll(STARTS_WITH_DASH_REPLACEMENT);
		tripHeadsign = ENDS_WITH_DASH_.matcher(tripHeadsign).replaceAll(ENDS_WITH_DASH_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 9L) {
			if (Arrays.asList( //
					"Outbound", //
					"North End" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("North End", mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 60L) {
			if (Arrays.asList( //
					"Armstrong Only", //
					"Enderby" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Enderby", mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s!", mTrip, mTripToMerge);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
