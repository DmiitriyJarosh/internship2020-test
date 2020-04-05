import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


data class Point(val latitude: Float, val longitude: Float)
data class Participants(val passengers: Collection<Person>, val drivers: Collection<Person>)
data class Person(val id: UUID, val finishPoint: Point)

fun main() {
    val (passengers, drivers) = readPoints()
    val startPoint = Point(0F, 0F)
    for (passenger in passengers) {
        val suggestedDrivers = suggestDrivers(passenger, drivers, startPoint)
        println("Passenger point: ${passenger.finishPoint.latitude}, ${passenger.finishPoint.longitude}")
        for (driver in suggestedDrivers) {
            println("  ${driver.finishPoint.latitude}, ${driver.finishPoint.longitude}")
        }
    }
}

fun suggestDrivers(passenger: Person, drivers: Collection<Person>, startPoint: Point): Collection<Person> {
    val passengerPoint = passenger.finishPoint
    val result = mutableListOf<Pair<Person, Float>>()
    val epsilon = 0.05 // it will be a part of driver class in future
    for (driver in drivers) {
        val driverPoint = driver.finishPoint
        val distance = abs(
                (driverPoint.longitude - startPoint.longitude) * passengerPoint.latitude
                        - (driverPoint.latitude - startPoint.latitude) * passengerPoint.longitude
                        + driverPoint.latitude * startPoint.longitude
                        - driverPoint.longitude * startPoint.latitude
        ) / sqrt(
                (driverPoint.longitude - startPoint.longitude).pow(2)
                        + (driverPoint.latitude - startPoint.latitude).pow(2)
        )

        if (distance <= epsilon) {
            result.add(driver to distance)
        }
    }

    result.sortBy { it.second }

    return result.map { it.first }
}

private fun readPoints(): Participants {
    val pathToResource = Paths.get(Point::class.java.getResource("latlons").toURI())
    val allPoints = Files.readAllLines(pathToResource).map { asPoint(it) }.shuffled()
    val passengers = allPoints.slice(0..9).map { Person(UUID.randomUUID(), it) }
    val drivers = allPoints.slice(10..19).map { Person(UUID.randomUUID(), it) }
    return Participants(passengers, drivers)
}

private fun asPoint(it: String): Point {
    val (lat, lon) = it.split(", ")
    return Point(lat.toFloat(), lon.toFloat())
}
