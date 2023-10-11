package com.gf.common.utils



/**
 * Clase usada para registrar la distancia y tiempo, además de obtener la velocidad del último kilometro
 */
class StatCounter() {

    var lastTimeGivenSpeed = 0

    companion object{
        const val LAST_KILOMETER = 1000
        const val UPDATE_SPEED_COOLDOWN = 15

        /**
         * Devuelve el tiempo formateado HH:mm:ss
         */
        fun formatTime(time: Int) : String{
            val hours = time / 3600
            val mins = (time % 3600) / 60
            val seconds = (time % 3600) % 60

            return "$hours:${String.format("%02d",mins)}:${String.format("%02d",seconds)}"
        }

    }

   val queue : MutableList<Pair<Int,Int>> = mutableListOf()

    // Estadísticas

    // Tiempo - Segundos
    private val totalTime = mutableListOf<Int>()

    // Distancia - Metros
    var totalDistance = 0

    val lastDistance : Int
        get() = queue.sumOf { it.first }

    val lastTime : Int
        get() =
            if (queue.isEmpty())
                1
            else
                queue.sumOf { it.second }


    /**
     * Añade un nuevo registro Distancia/Tiempo, si este es mayor que meterCapacity
     * va actualizando o eliminando los primeros valores de la queue
     * @param distance Distancia en metros
     * @param time Tiempo en segundos
     */
    fun add(distance: Int, time: Int){
        // Añadimos dist / tiem a la cola
        queue.add(Pair(distance,time))
        addTotal(distance,time)

        while (lastDistance > LAST_KILOMETER){
            var firstD = queue.first().first
            var firstT = queue.first().second
            val differD = lastDistance - LAST_KILOMETER
            if (firstD <= differD){
                queue.removeFirst()
            }else {
                firstT = firstT * (firstD-differD) / firstD
                firstD -= differD
                queue[0] = Pair(firstD,firstT)
            }
        }

    }

    /**
     * Se encarga de añadir al total de minutos y segundos
     */
    private fun addTotal(distance: Int,time: Int){
        if (totalDistance + distance < LAST_KILOMETER){
            totalDistance += distance
            if (totalTime.isEmpty())
                totalTime.add(time)
            else
                totalTime[totalTime.size-1] += time
        }
        else{
            // Distancia añadida no se expande entre 2 o más km distintos
            var currentKilometer = totalDistance / LAST_KILOMETER
            var updatedKilometer = totalDistance + distance / LAST_KILOMETER
            var remDistance = distance
            var remTime = time
            val timePerM = time * LAST_KILOMETER / distance
            while (currentKilometer <= updatedKilometer){
                if (currentKilometer == updatedKilometer){
                    totalDistance += distance
                    if (totalTime.size - 1 == currentKilometer)
                        totalTime[currentKilometer] += time
                    else
                        totalTime.add(time)
                }
                else{
                    val metersToKm = LAST_KILOMETER - (totalDistance % LAST_KILOMETER)
                    val secondsToKm = metersToKm * timePerM

                    remDistance -= metersToKm
                    remTime -= secondsToKm

                    totalDistance+=metersToKm
                    if (totalTime.size - 1 == currentKilometer)
                        totalTime[currentKilometer] += secondsToKm
                    else
                        totalTime.add(secondsToKm)
                }

                currentKilometer = totalDistance / LAST_KILOMETER
                updatedKilometer = totalDistance + distance / LAST_KILOMETER
            }

        }
    }

    fun getTime() = totalTime.sum()

    fun distanceKm() : String = String.format("%.2f",totalDistance / 3600.0)





}