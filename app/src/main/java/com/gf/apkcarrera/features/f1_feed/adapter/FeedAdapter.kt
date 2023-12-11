package com.gf.apkcarrera.features.f1_feed.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cotesa.common.extensions.toBitmap
import com.gf.apkcarrera.R
import com.gf.common.entity.activity.ActivityModel
import com.gf.common.entity.activity.ActivityType
import com.gf.common.entity.activity.ActivityType.BIKE
import com.gf.common.entity.activity.ActivityType.RUN
import com.gf.common.entity.activity.ActivityType.WALK
import com.gf.common.entity.activity.RegistryPoint
import com.gf.common.entity.feed.FeedImage
import com.gf.common.entity.user.UserModel
import com.gf.common.extensions.adjustCamera
import com.gf.common.extensions.assignAnimatedAdapter
import com.gf.common.extensions.invisible
import com.gf.common.extensions.paintPolyline
import com.gf.common.extensions.visible
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import net.cachapa.expandablelayout.ExpandableLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedAdapter (
    val userId : String,
    val onImageClick : (images:List<FeedImage>, position : Int) -> Unit,
    val onProfileClick : (user : UserModel) -> Unit
) :
    PagingDataAdapter<Pair<ActivityModel,UserModel>, ActivityViewHolder>(ActivityModelComparator) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity,parent,false)

        return ActivityViewHolder(view,userId,onImageClick,onProfileClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = getItem(position)
        // Note that item can be null. ViewHolder must support binding a
        // null item as a placeholder.
        holder.bind(item)
    }


}

class ActivityViewHolder(view: View,
                         val userId : String,
                         val onImageClick : (images:List<FeedImage>, position : Int) -> Unit,
                         val onProfileClick : (user : UserModel) -> Unit) : RecyclerView.ViewHolder(view) {

    private lateinit var googleMap : GoogleMap
    var mapView : MapView? = null
    fun bind(item:Pair<ActivityModel,UserModel>?){
        val activity = item!!.first
        val user = item.second

        val context = itemView.context

        val tvDescription = this.itemView.findViewById<TextView>(R.id.tv_message)
        val ivImage = this.itemView.findViewById<ShapeableImageView>(R.id.iv_profile_pic)
        val tvName = this.itemView.findViewById<TextView>(R.id.tv_name)
        val tvDate = this.itemView.findViewById<TextView>(R.id.tv_date)
        val tvStatDistance = this.itemView.findViewById<TextView>(R.id.tv_stat_distance)
        val tvStatRythm = this.itemView.findViewById<TextView>(R.id.tv_stat_rythm)
        val tvStatTime = this.itemView.findViewById<TextView>(R.id.tv_stat_time)
        val tvFav1 = this.itemView.findViewById<TextView>(R.id.tv_fav1)
        val btFav1 = this.itemView.findViewById<Button>(R.id.bt_fav1)
        val blueBar = this.itemView.findViewById<View>(R.id.blue_bar)
        val whiteBar = this.itemView.findViewById<View>(R.id.white_bar)
        val tvFav2 = this.itemView.findViewById<TextView>(R.id.tv_fav2)
        val btFav2 = this.itemView.findViewById<Button>(R.id.bt_fav2)
        val btDeploy = this.itemView.findViewById<AppCompatImageButton>(R.id.bt_deploy)
        val ivType = this.itemView.findViewById<ImageView>(R.id.iv_type)
        val rvList = this.itemView.findViewById<RecyclerView>(R.id.rv_list)
        val expandable = this.itemView.findViewById<ExpandableLayout>(R.id.expandable)
        mapView = this.itemView.findViewById(R.id.map)
        val cvMap = this.itemView.findViewById<MaterialCardView>(R.id.cv_map)


        // 0. Desplegable
        if (absoluteAdapterPosition == 0)
            expandable.expand(false)
        else
            expandable.collapse(false)

        val arrow = if (expandable.isExpanded)
            com.gf.common.R.drawable.small_arrow_up
        else
            com.gf.common.R.drawable.small_arrow_down

        btDeploy.setImageResource(arrow)


        whiteBar.setOnClickListener {
            expandable.toggle()

            val resource = if (expandable.isExpanded)
                com.gf.common.R.drawable.small_arrow_up
            else
                com.gf.common.R.drawable.small_arrow_down

            btDeploy.setImageResource(resource)
        }

        // 1. Nombre
        tvName.text = user.name

        // 2. Fecha
        tvDate.text = getTimeStamp(activity.timestamp * 1000)

        // 3. Foto perfil
        ivImage.setImageBitmap(user.picture.toBitmap())

        // 4. Descripcion
        tvDescription.text = activity.title

        // 5. Tipo actividad
        val resource = when(activity.type){
            WALK -> AppCompatResources.getDrawable(context,com.gf.common.R.drawable.icon_walk)
            RUN -> AppCompatResources.getDrawable(context,com.gf.common.R.drawable.icon_run)
            BIKE -> AppCompatResources.getDrawable(context,com.gf.common.R.drawable.icon_bike)
        }
        ivType.setImageDrawable(resource)

        // 6. Distancia
        tvStatDistance.text = formatDistance(activity.distance)

        // 7. Ritmo / velocidad
        val time = activity.time.sum()
        val speed = ((activity.distance + 0.0) / time)
        tvStatRythm.text = formatSpeed(speed,activity.type)

        // 8. Tiempo
        tvStatTime.text = formatTime(time)

        // 9. Imágenes
        if (activity.images.isNotEmpty()){
            rvList.assignAnimatedAdapter(FeedImagesAdapter(activity.images.map { FeedImage(
                id = activity.images.indexOf(it),
                url = it)
            },
                onImageClick = onImageClick),
                com.gf.common.R.anim.animation_layout_fade_in,
                false)
        }
        else
            rvList.invisible()


        // 10. Mapa con la ruta
        var points : MutableList<List<RegistryPoint>> = mutableListOf()
        val actPoints = activity.points as List<HashMap<String, ArrayList<HashMap<String, Any>>>>
        actPoints.forEach { partial ->
            val partialPoints = mutableListOf<RegistryPoint>()
            partial["registryPoints"]?.forEach{ point ->
                val actLatLng = point["latLng"] as HashMap<String,Double>
                val regPoint = RegistryPoint(
                    altitude = (point["altitude"] as Long).toInt(),
                    distance = (point["distance"] as Long).toInt(),
                    latLng = LatLng(actLatLng["latitude"]!!,actLatLng["longitude"]!!),
                    time = (point["time"] as Long).toInt()
                )
                partialPoints.add(regPoint)

            }
            points.add(partialPoints)
        }

        if (points.flatten().count() > 2){
            cvMap?.visible()
            loadMap(mapView!!,points)
        }
        else
            cvMap?.invisible()

        // 11. Click perfil

        val color = if (userId == user.uid)
            itemView.context.getColor(com.gf.common.R.color.statistic4)
        else
            itemView.context.getColor(com.gf.common.R.color.blue_primary)

        blueBar.backgroundTintList = ColorStateList.valueOf(color)
        blueBar.setOnClickListener { onProfileClick(user) }


        // TODO Implementar sistema de favoritos
    }

    private fun loadMap(map:MapView,points: List<List<RegistryPoint>>) {
        map.onCreate(null)
        map.onResume()
        map.getMapAsync{
            MapsInitializer.initialize(itemView.context);
            googleMap = it;
            googleMap.paintPolyline(map.context,points)
            googleMap.adjustCamera(points)
        }
    }

    fun getTimeStamp(timestampMillis: Long): String {
        val date = Date(timestampMillis)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun formatSpeed(speed:Double,activityType : ActivityType) : String{
        return when (activityType){
            BIKE ->{
                val kmh = (speed * 3.6).toInt()
                "$kmh km/h"
            }
            else ->{
                val minkm = ((speed * 3.6)/60)

                // Calcula los minutos y segundos
                val minutos = (minkm).toInt()
                val segundos = ((minkm * 60) % 60).toInt()

                // Formatea la cadena en "mm:ss"
                String.format("%02d:%02d min/km", minutos, segundos)
            }
        }
    }

    private fun formatTime(time: Int) : String{
        val hours = time / 3600
        val mins = (time % 3600) / 60

        return "${hours}h ${String.format("%02d",mins)}m"
    }

    private fun formatDistance(distance: Int) : String{
        return if (distance < 1000)
            "$distance m"
        else
            "${distance/1000} km"

    }


}
