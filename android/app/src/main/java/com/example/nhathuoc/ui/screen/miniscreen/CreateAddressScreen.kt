package com.example.nhathuoc.ui.screen.miniscreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.AddAddressRequest
import com.example.nhathuoc.data.model.AddressProvince
import com.example.nhathuoc.data.model.AddressWard
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.viewmodel.AddressPickerViewModel
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.tan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAddressScreen(
    onSave: (AddAddressRequest) -> Unit,
    onBack: () -> Unit,
    initialAddress: UserAddress? = null,
    addressPickerViewModel: AddressPickerViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val pickerState by addressPickerViewModel.state.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var recipientName by remember(initialAddress?.id) { mutableStateOf(initialAddress?.recipientName.orEmpty()) }
    var recipientPhone by remember(initialAddress?.id) { mutableStateOf(initialAddress?.recipientPhone.orEmpty()) }
    var streetAddress by remember(initialAddress?.id) {
        mutableStateOf(
            initialAddress?.let {
                val full = it.fullAddress
                if (!full.isNullOrBlank()) {
                    extractStreetAddress(full, it.ward.orEmpty(), it.district.orEmpty(), it.province.orEmpty())
                } else {
                    it.address.ifBlank { "" }
                }
            }.orEmpty()
        )
    }
    var ward by remember(initialAddress?.id) { mutableStateOf(initialAddress?.ward.orEmpty()) }
    var wardCode by remember(initialAddress?.id) { mutableStateOf(initialAddress?.wardCode) }
    var district by remember(initialAddress?.id) { mutableStateOf(initialAddress?.district.orEmpty()) }
    var province by remember(initialAddress?.id) { mutableStateOf(initialAddress?.province.orEmpty()) }
    var provinceCode by remember(initialAddress?.id) { mutableStateOf(initialAddress?.provinceCode) }
    var latitude by remember(initialAddress?.id) { mutableStateOf(initialAddress?.latitude) }
    var longitude by remember(initialAddress?.id) { mutableStateOf(initialAddress?.longitude) }
    var locationSource by remember(initialAddress?.id) { mutableStateOf(initialAddress?.locationSource ?: "MANUAL") }
    var type by remember(initialAddress?.id) { mutableStateOf(initialAddress?.type ?: "home") }
    var isDefault by remember(initialAddress?.id) { mutableStateOf(initialAddress?.isDefault ?: false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var pendingMapWardNames by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(initialAddress?.provinceCode, pickerState.provinces) {
        val currentProvinceCode = initialAddress?.provinceCode ?: return@LaunchedEffect
        val matched = pickerState.provinces.firstOrNull { it.code == currentProvinceCode }
        if (matched != null && pickerState.selectedProvince?.code != matched.code) {
            addressPickerViewModel.selectProvince(matched)
        }
    }

    LaunchedEffect(pendingMapWardNames, pickerState.wards) {
        if (pendingMapWardNames.isEmpty()) return@LaunchedEffect
        val matchedWard = matchAdministrativeName(
            options = pickerState.wards,
            candidates = pendingMapWardNames,
            selector = AddressWard::name
        ) ?: return@LaunchedEffect

        ward = matchedWard.name
        wardCode = matchedWard.code
        district = matchedWard.districtName.orEmpty()
        addressPickerViewModel.selectWard(matchedWard)
        pendingMapWardNames = emptyList()
    }

    fun applyLocation(lat: Double, lng: Double, source: String) {
        latitude = lat
        longitude = lng
        locationSource = source

        coroutineScope.launch {
            val hint = reverseAddressHint(context, lat, lng) ?: return@launch
            val matchedProvince = matchAdministrativeName(
                options = pickerState.provinces,
                candidates = hint.provinceNames,
                selector = AddressProvince::name
            ) ?: return@launch

            province = matchedProvince.name
            provinceCode = matchedProvince.code
            ward = ""
            wardCode = null
            district = ""
            pendingMapWardNames = hint.wardNames
            addressPickerViewModel.selectProvince(matchedProvince)
        }
    }

    @SuppressLint("MissingPermission")
    fun readCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    applyLocation(location.latitude, location.longitude, "GPS")
                } else {
                    Toast.makeText(context, "Chưa lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show()
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            readCurrentLocation()
        } else {
            Toast.makeText(context, "Cần quyền vị trí để định vị chính xác", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCurrentLocation() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            readCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = if (initialAddress == null) "Thêm địa chỉ mới" else "Sửa địa chỉ",
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val phone = recipientPhone.filter(Char::isDigit)
                        when {
                            recipientName.isBlank() -> errorText = "Vui lòng nhập tên người nhận"
                            phone.length < 9 -> errorText = "Số điện thoại không hợp lệ"
                            streetAddress.isBlank() -> errorText = "Vui lòng nhập số nhà, tên đường"
                            province.isBlank() -> errorText = "Vui lòng chọn tỉnh/thành phố"
                            ward.isBlank() -> errorText = "Vui lòng chọn xã/phường"
                            else -> {
                                errorText = null
                                onSave(
                                    AddAddressRequest(
                                        type = type,
                                        recipientName = recipientName.trim(),
                                        recipientPhone = phone,
                                        fullAddress = streetAddress.trim(),
                                        ward = ward.trim(),
                                        wardCode = wardCode,
                                        district = district.trim(),
                                        province = province.trim(),
                                        provinceCode = provinceCode,
                                        latitude = latitude,
                                        longitude = longitude,
                                        locationSource = locationSource,
                                        isDefault = isDefault
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (initialAddress == null) "Lưu địa chỉ" else "Cập nhật địa chỉ")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            pickerState.error?.let {
                Text(
                    text = "$it. Bạn vẫn có thể nhập tay.",
                    color = Color(0xFFB45309),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Người nhận") },
                singleLine = true
            )

            OutlinedTextField(
                value = recipientPhone,
                onValueChange = { recipientPhone = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số điện thoại") },
                singleLine = true
            )

            AddressDropdownField(
                label = "Tỉnh/Thành phố",
                value = province,
                isLoading = pickerState.isLoadingProvinces,
                options = pickerState.provinces,
                optionText = { it.name },
                onValueChange = {
                    province = it
                    provinceCode = null
                    ward = ""
                    wardCode = null
                    district = ""
                },
                onOptionSelected = {
                    province = it.name
                    provinceCode = it.code
                    ward = ""
                    wardCode = null
                    district = ""
                    addressPickerViewModel.selectProvince(it)
                }
            )

            AddressDropdownField(
                label = "Xã/Phường",
                value = ward,
                isLoading = pickerState.isLoadingWards,
                options = pickerState.wards,
                optionText = { item ->
                    if (item.districtName.isNullOrBlank()) item.name else "${item.name} - ${item.districtName}"
                },
                onValueChange = {
                    ward = it
                    wardCode = null
                    district = ""
                },
                onOptionSelected = {
                    ward = it.name
                    wardCode = it.code
                    district = it.districtName.orEmpty()
                    addressPickerViewModel.selectWard(it)
                }
            )

            if (district.isNotBlank()) {
                Text(
                    text = "Khu vực: $district",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = streetAddress,
                onValueChange = { streetAddress = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số nhà, tên đường") },
                singleLine = true
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tọa độ giao hàng", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (latitude != null && longitude != null) {
                                    "${formatCoordinate(latitude!!)}, ${formatCoordinate(longitude!!)} · $locationSource"
                                } else {
                                    "Chưa chọn, phí ship sẽ tính theo tỉnh/xã"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Button(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Chọn trên bản đồ")
                    }
                }
            }

            Text(
                text = "Loại địa chỉ",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    "home" to "Nhà riêng",
                    "work" to "Công ty",
                    "other" to "Khác"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = type == value,
                        onClick = { type = value },
                        label = { Text(label) }
                    )
                }
            }

            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đặt làm mặc định", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Ưu tiên dùng địa chỉ này khi thanh toán", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDefault, onCheckedChange = { isDefault = it })
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    if (showMapPicker) {
        OsmMapPickerDialog(
            initialLat = latitude ?: SHOP_LAT,
            initialLng = longitude ?: SHOP_LNG,
            onCoordinatePicked = { lat, lng -> applyLocation(lat, lng, "MAP") },
            onRequestCurrentLocation = { requestCurrentLocation() },
            onDismiss = { showMapPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> AddressDropdownField(
    label: String,
    value: String,
    isLoading: Boolean,
    options: List<T>,
    optionText: (T) -> String,
    onValueChange: (String) -> Unit,
    onOptionSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredOptions = remember(value, options) {
        if (value.isBlank()) {
            options.take(80)
        } else {
            options.filter { optionText(it).contains(value, ignoreCase = true) }.take(80)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredOptions.isNotEmpty(),
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            singleLine = true,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredOptions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionText(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun OsmMapPickerDialog(
    initialLat: Double,
    initialLng: Double,
    onCoordinatePicked: (Double, Double) -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    var pickedLat by remember(initialLat, initialLng) { mutableDoubleStateOf(initialLat) }
    var pickedLng by remember(initialLat, initialLng) { mutableDoubleStateOf(initialLng) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn vị trí giao hàng") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chạm vào bản đồ để đặt ghim. Nguồn bản đồ: OpenStreetMap.",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall
                )
                OsmdroidMapPicker(
                    lat = pickedLat,
                    lng = pickedLng,
                    onLocateClick = onRequestCurrentLocation,
                    onCoordinatePicked = { lat, lng ->
                        pickedLat = lat
                        pickedLng = lng
                    }
                )
                Text(
                    text = "${formatCoordinate(pickedLat)}, ${formatCoordinate(pickedLng)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCoordinatePicked(pickedLat, pickedLng)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Dùng vị trí này")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun OsmdroidMapPicker(
    lat: Double,
    lng: Double,
    onLocateClick: () -> Unit,
    onCoordinatePicked: (Double, Double) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                val osmConfig = Configuration.getInstance()
                osmConfig.load(
                    viewContext,
                    viewContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                )
                osmConfig.userAgentValue = "${viewContext.packageName}/1.0"
                osmConfig.osmdroidBasePath = File(viewContext.cacheDir, "osmdroid").apply { mkdirs() }
                osmConfig.osmdroidTileCache = File(osmConfig.osmdroidBasePath, "tiles").apply { mkdirs() }

                val initialPoint = GeoPoint(lat, lng)
                MapView(viewContext).apply {
                    mapView = this
                    setTileSource(CARTO_VOYAGER_TILE_SOURCE)
                    setUseDataConnection(true)
                    setMultiTouchControls(true)
                    minZoomLevel = OSM_MIN_ZOOM.toDouble()
                    maxZoomLevel = OSM_MAX_ZOOM.toDouble()
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(OSM_DEFAULT_ZOOM.toDouble())
                    controller.setCenter(initialPoint)

                    val marker = Marker(this).apply {
                        position = initialPoint
                        icon = BitmapDrawable(resources, createDeliveryPinBitmap())
                        title = "Vị trí giao hàng"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }

                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                            if (point == null) return false
                            marker.position = point
                            controller.animateTo(point)
                            onCoordinatePicked(point.latitude, point.longitude)
                            invalidate()
                            return true
                        }

                        override fun longPressHelper(point: GeoPoint?): Boolean = false
                    })

                    overlays.add(eventsOverlay)
                    overlays.add(marker)
                    onResume()
                }
            },
            update = { map ->
                val point = GeoPoint(lat, lng)
                map.overlays.filterIsInstance<Marker>().firstOrNull()?.position = point
                map.controller.setCenter(point)
                map.invalidate()
            }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = onLocateClick,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Định vị hiện tại",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { mapView?.controller?.zoomIn() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Text("+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { mapView?.controller?.zoomOut() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Text("-", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = "Kéo, zoom và chạm để đặt ghim - OpenStreetMap",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun OsmTilePicker(
    lat: Double,
    lng: Double,
    onLocateClick: () -> Unit,
    onCoordinatePicked: (Double, Double) -> Unit
) {
    var centerLat by remember(lat, lng) { mutableDoubleStateOf(lat) }
    var centerLng by remember(lat, lng) { mutableDoubleStateOf(lng) }
    var zoom by remember(lat, lng) { mutableStateOf(OSM_DEFAULT_ZOOM) }
    var dragOffset by remember(lat, lng, zoom) { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    fun updateCenterFromPixel(pixelX: Double, pixelY: Double) {
        dragOffset = Offset.Zero
        centerLat = pixelYToLat(pixelY, zoom).coerceIn(OSM_MIN_LAT, OSM_MAX_LAT)
        centerLng = pixelXToLon(pixelX, zoom)
        onCoordinatePicked(centerLat, centerLng)
    }

    fun commitDragOffset() {
        if (dragOffset == Offset.Zero) return
        val centerPixelX = lonToPixelX(centerLng, zoom) - dragOffset.x
        val centerPixelY = latToPixelY(centerLat, zoom) - dragOffset.y
        updateCenterFromPixel(centerPixelX, centerPixelY)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE5E7EB))
            .pointerInput(centerLat, centerLng, zoom) {
                detectDragGestures(
                    onDragEnd = { commitDragOffset() },
                    onDragCancel = { dragOffset = Offset.Zero }
                ) { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount
                }
            }
            .pointerInput(centerLat, centerLng, zoom) {
                detectTapGestures { tap ->
                    val width = size.width.toDouble()
                    val height = size.height.toDouble()
                    val centerPixelX = lonToPixelX(centerLng, zoom)
                    val centerPixelY = latToPixelY(centerLat, zoom)
                    updateCenterFromPixel(
                        pixelX = centerPixelX + tap.x - width / 2.0,
                        pixelY = centerPixelY + tap.y - height / 2.0
                    )
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toDouble().coerceAtLeast(1.0)
        val heightPx = constraints.maxHeight.toDouble().coerceAtLeast(1.0)
        val tileDp = with(density) { OSM_TILE_SIZE_PX.toFloat().toDp() }
        val worldTiles = 1 shl zoom
        val centerPixelX = lonToPixelX(centerLng, zoom)
        val centerPixelY = latToPixelY(centerLat, zoom)
        val startTileX = floor((centerPixelX - widthPx / 2.0) / OSM_TILE_SIZE_PX).toInt() - 1
        val endTileX = floor((centerPixelX + widthPx / 2.0) / OSM_TILE_SIZE_PX).toInt() + 1
        val startTileY = floor((centerPixelY - heightPx / 2.0) / OSM_TILE_SIZE_PX).toInt() - 1
        val endTileY = floor((centerPixelY + heightPx / 2.0) / OSM_TILE_SIZE_PX).toInt() + 1

        for (tileX in startTileX..endTileX) {
            for (tileY in startTileY..endTileY) {
                if (tileY !in 0 until worldTiles) continue
                val wrappedX = ((tileX % worldTiles) + worldTiles) % worldTiles
                val leftPx = tileX * OSM_TILE_SIZE_PX - centerPixelX + widthPx / 2.0 + dragOffset.x
                val topPx = tileY * OSM_TILE_SIZE_PX - centerPixelY + heightPx / 2.0 + dragOffset.y
                AsyncImage(
                    model = osmTileUrl(zoom, wrappedX, tileY),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                        .size(tileDp)
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(46.dp)
                .offset(y = (-18).dp)
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = onLocateClick,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Định vị hiện tại",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        dragOffset = Offset.Zero
                        zoom = (zoom + 1).coerceAtMost(OSM_MAX_ZOOM)
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    Text("+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        dragOffset = Offset.Zero
                        zoom = (zoom - 1).coerceAtLeast(OSM_MIN_ZOOM)
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    Text("-", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = "Chạm vào bản đồ để đặt ghim - dữ liệu OSM/CARTO",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

private fun osmPickerHtml(lat: Double, lng: Double): String {
    return """
        <!doctype html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                html, body { height: 100%; margin: 0; padding: 0; background: #f1f5f9; overflow: hidden; }
                #map { position: relative; width: 100%; height: 100%; overflow: hidden; background: #e5e7eb; touch-action: manipulation; }
                .tile { position: absolute; width: 256px; height: 256px; }
                .pin {
                    position: absolute; left: 50%; top: 50%; width: 28px; height: 28px;
                    margin-left: -14px; margin-top: -28px; border-radius: 18px 18px 18px 0;
                    transform: rotate(-45deg); background: #2e7d32; box-shadow: 0 2px 8px rgba(0,0,0,.25);
                    z-index: 10;
                }
                .pin:after {
                    content: ''; position: absolute; width: 10px; height: 10px; border-radius: 50%;
                    background: white; left: 9px; top: 9px;
                }
                .hint {
                    position: absolute; left: 10px; bottom: 10px; right: 10px; z-index: 11;
                    background: rgba(255,255,255,.92); color: #1b5e20; font: 13px sans-serif;
                    border-radius: 12px; padding: 8px 10px;
                }
            </style>
        </head>
        <body>
            <div id="map"><div class="pin"></div><div class="hint">Chạm vào bản đồ để đặt ghim</div></div>
            <script>
                let centerLat = $lat;
                let centerLng = $lng;
                const zoom = 15;
                const tileSize = 256;
                const map = document.getElementById('map');

                function lon2tile(lon, z) { return (lon + 180) / 360 * Math.pow(2, z); }
                function lat2tile(lat, z) {
                    const rad = lat * Math.PI / 180;
                    return (1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2 * Math.pow(2, z);
                }
                function tile2lon(x, z) { return x / Math.pow(2, z) * 360 - 180; }
                function tile2lat(y, z) {
                    const n = Math.PI - 2 * Math.PI * y / Math.pow(2, z);
                    return 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
                }

                function notify() {
                    if (window.AndroidPicker) {
                        window.AndroidPicker.onPick(centerLat, centerLng);
                    }
                }

                function render() {
                    const w = map.clientWidth || 320;
                    const h = map.clientHeight || 360;
                    const cx = lon2tile(centerLng, zoom) * tileSize;
                    const cy = lat2tile(centerLat, zoom) * tileSize;
                    const startX = Math.floor((cx - w / 2) / tileSize) - 1;
                    const endX = Math.floor((cx + w / 2) / tileSize) + 1;
                    const startY = Math.floor((cy - h / 2) / tileSize) - 1;
                    const endY = Math.floor((cy + h / 2) / tileSize) + 1;
                    map.querySelectorAll('.tile').forEach(e => e.remove());
                    for (let x = startX; x <= endX; x++) {
                        for (let y = startY; y <= endY; y++) {
                            const img = document.createElement('img');
                            img.className = 'tile';
                            img.src = 'https://tile.openstreetmap.org/' + zoom + '/' + x + '/' + y + '.png';
                            img.style.left = (x * tileSize - cx + w / 2) + 'px';
                            img.style.top = (y * tileSize - cy + h / 2) + 'px';
                            map.insertBefore(img, map.firstChild);
                        }
                    }
                }

                map.addEventListener('click', function(e) {
                    const rect = map.getBoundingClientRect();
                    const dx = e.clientX - rect.left - rect.width / 2;
                    const dy = e.clientY - rect.top - rect.height / 2;
                    const cx = lon2tile(centerLng, zoom) * tileSize + dx;
                    const cy = lat2tile(centerLat, zoom) * tileSize + dy;
                    centerLng = tile2lon(cx / tileSize, zoom);
                    centerLat = tile2lat(cy / tileSize, zoom);
                    render();
                    notify();
                });
                window.addEventListener('resize', render);
                render();
                notify();
            </script>
        </body>
        </html>
    """.trimIndent()
}

private fun createDeliveryPinBitmap(): Bitmap {
    val width = 72
    val height = 88
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(70, 0, 0, 0)
    }

    val pinPath = Path().apply {
        moveTo(36f, 84f)
        cubicTo(30f, 70f, 12f, 56f, 12f, 34f)
        cubicTo(12f, 15f, 23f, 6f, 36f, 6f)
        cubicTo(49f, 6f, 60f, 15f, 60f, 34f)
        cubicTo(60f, 56f, 42f, 70f, 36f, 84f)
        close()
    }

    canvas.drawOval(22f, 74f, 50f, 86f, shadowPaint)
    paint.color = AndroidColor.rgb(46, 125, 50)
    canvas.drawPath(pinPath, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    paint.color = AndroidColor.WHITE
    canvas.drawPath(pinPath, paint)
    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.WHITE
    canvas.drawCircle(36f, 34f, 12f, paint)
    paint.color = AndroidColor.rgb(46, 125, 50)
    canvas.drawCircle(36f, 34f, 6f, paint)

    return bitmap
}

private data class ReverseAddressHint(
    val provinceNames: List<String>,
    val wardNames: List<String>
)

@Suppress("DEPRECATION")
private suspend fun reverseAddressHint(context: Context, lat: Double, lng: Double): ReverseAddressHint? =
    withContext(Dispatchers.IO) {
        val androidHint = runCatching {
            if (!Geocoder.isPresent()) return@runCatching null
            val address = Geocoder(context, Locale("vi", "VN"))
                .getFromLocation(lat, lng, 1)
                ?.firstOrNull()
                ?: return@runCatching null

            ReverseAddressHint(
                provinceNames = compactCandidates(
                    address.adminArea,
                    address.locality,
                    address.subAdminArea,
                    address.getAddressLine(0)
                ),
                wardNames = compactCandidates(
                    address.subLocality,
                    address.featureName,
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.getAddressLine(0)
                )
            )
        }.getOrNull()

        val osmHint = reverseAddressHintFromOsm(lat, lng)
        mergeReverseAddressHints(androidHint, osmHint)
    }

private fun reverseAddressHintFromOsm(lat: Double, lng: Double): ReverseAddressHint? = runCatching {
    val url = URL(
        "https://nominatim.openstreetmap.org/reverse" +
            "?format=jsonv2&lat=$lat&lon=$lng&zoom=18&addressdetails=1&accept-language=vi"
    )
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 3500
        readTimeout = 3500
        setRequestProperty("User-Agent", "MedStore-Android/1.0 (address-picker)")
        setRequestProperty("Accept", "application/json")
    }

    try {
        if (connection.responseCode !in 200..299) return@runCatching null
        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val address = JSONObject(body).optJSONObject("address") ?: return@runCatching null

        ReverseAddressHint(
            provinceNames = compactCandidates(
                address.optStringOrNull("city"),
                address.optStringOrNull("state"),
                address.optStringOrNull("province"),
                address.optStringOrNull("region")
            ),
            wardNames = compactCandidates(
                address.optStringOrNull("suburb"),
                address.optStringOrNull("quarter"),
                address.optStringOrNull("neighbourhood"),
                address.optStringOrNull("city_district"),
                address.optStringOrNull("borough"),
                address.optStringOrNull("village"),
                address.optStringOrNull("town"),
                address.optStringOrNull("hamlet"),
                address.optStringOrNull("municipality")
            )
        )
    } finally {
        connection.disconnect()
    }
}.getOrNull()

private fun mergeReverseAddressHints(
    androidHint: ReverseAddressHint?,
    osmHint: ReverseAddressHint?
): ReverseAddressHint? {
    val provinceNames = compactCandidates(
        *(androidHint?.provinceNames.orEmpty() + osmHint?.provinceNames.orEmpty()).toTypedArray()
    )
    val wardNames = compactCandidates(
        *(osmHint?.wardNames.orEmpty() + androidHint?.wardNames.orEmpty()).toTypedArray()
    )
    if (provinceNames.isEmpty() && wardNames.isEmpty()) return null
    return ReverseAddressHint(provinceNames = provinceNames, wardNames = wardNames)
}

private fun compactCandidates(vararg values: String?): List<String> =
    values.mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }.distinct()

private fun JSONObject.optStringOrNull(name: String): String? =
    optString(name, "").trim().takeIf(String::isNotBlank)

private fun <T> matchAdministrativeName(
    options: List<T>,
    candidates: List<String?>,
    selector: (T) -> String
): T? {
    val normalizedCandidates = candidates
        .mapNotNull { normalizeAdministrativeName(it).takeIf(String::isNotBlank) }

    return options.firstOrNull { option ->
        val normalizedOption = normalizeAdministrativeName(selector(option))
        normalizedCandidates.any { candidate ->
            normalizedOption == candidate ||
                normalizedOption.contains(candidate) ||
                candidate.contains(normalizedOption)
        }
    }
}

private fun normalizeAdministrativeName(value: String?): String {
    if (value.isNullOrBlank()) return ""
    val noAccent = Normalizer.normalize(value.lowercase(Locale("vi", "VN")), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .replace('đ', 'd')
    return noAccent
        .replace("\\b(tinh|thanh pho|tp|quan|huyen|thi xa|xa|phuong|thi tran)\\b".toRegex(), " ")
        .replace("[^a-z0-9]+".toRegex(), " ")
        .trim()
}

private const val OSM_DEFAULT_ZOOM = 19
private const val OSM_MIN_ZOOM = 6
private const val OSM_MAX_ZOOM = 19
private val CARTO_VOYAGER_TILE_SOURCE = object : OnlineTileSourceBase(
    "Carto Voyager",
    0,
    20,
    256,
    ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl +
            MapTileIndex.getZoom(pMapTileIndex) + "/" +
            MapTileIndex.getX(pMapTileIndex) + "/" +
            MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}
private const val OSM_TILE_SIZE_PX = 256.0
private const val OSM_MIN_LAT = -85.05112878
private const val OSM_MAX_LAT = 85.05112878

private fun osmTileUrl(zoom: Int, x: Int, y: Int): String {
    return "https://a.basemaps.cartocdn.com/rastertiles/voyager/$zoom/$x/$y.png"
}

private fun lonToPixelX(lon: Double, zoom: Int): Double {
    val worldSize = (1 shl zoom) * OSM_TILE_SIZE_PX
    return (lon + 180.0) / 360.0 * worldSize
}

private fun latToPixelY(lat: Double, zoom: Int): Double {
    val clamped = lat.coerceIn(OSM_MIN_LAT, OSM_MAX_LAT)
    val rad = clamped * PI / 180.0
    val worldSize = (1 shl zoom) * OSM_TILE_SIZE_PX
    return (1.0 - ln(tan(rad) + 1.0 / cos(rad)) / PI) / 2.0 * worldSize
}

private fun pixelXToLon(pixelX: Double, zoom: Int): Double {
    val worldSize = (1 shl zoom) * OSM_TILE_SIZE_PX
    return pixelX / worldSize * 360.0 - 180.0
}

private fun pixelYToLat(pixelY: Double, zoom: Int): Double {
    val worldSize = (1 shl zoom) * OSM_TILE_SIZE_PX
    val n = PI - 2.0 * PI * pixelY / worldSize
    return 180.0 / PI * atan(0.5 * (exp(n) - exp(-n)))
}

private fun formatCoordinate(value: Double): String {
    return String.format(Locale.US, "%.6f", value)
}

private fun extractStreetAddress(fullAddress: String, ward: String, district: String, province: String): String {
    var street = fullAddress.trim()
    listOf(province, district, ward)
        .filter { it.isNotBlank() }
        .forEach { part ->
            street = street.removeSuffix(", $part").trim()
        }
    return street.ifBlank { fullAddress }
}

private const val SHOP_LAT = 20.9802
private const val SHOP_LNG = 105.7870
