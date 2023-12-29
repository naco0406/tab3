package com.example.madcamp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.app.Dialog
import androidx.cardview.widget.CardView

class Tab2 : Fragment() {
    override fun onResume() {
        super.onResume()
        val rootView = view
        val gridLayout = rootView?.findViewById<GridLayout>(R.id.gridview)
        gridLayout?.requestLayout()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setContentView(R.layout.fragment_tab2)
        val gridLayout: GridLayout = view.findViewById(R.id.gridview)
        val imageResIds = listOf(
            R.drawable.image_choonsik1,
            R.drawable.image_choonsik2,
            R.drawable.image_choonsik3,
            R.drawable.image_choonsik4,
            R.drawable.image_choonsik5,
            R.drawable.image_cat1,
            R.drawable.image_cat2,
            R.drawable.image_cat3,
            R.drawable.image_cat4,
            R.drawable.image_cat5,
            R.drawable.image_choonsik1,
            R.drawable.image_choonsik2,
            R.drawable.image_choonsik3,
            R.drawable.image_choonsik4,
            R.drawable.image_choonsik5,
            R.drawable.image_cat1,
            R.drawable.image_cat2,
            R.drawable.image_cat3,
            R.drawable.image_cat4,
            R.drawable.image_cat5,
        )
        imageResIds.forEachIndexed() { index, resId ->
//            val cardView = CardView(requireContext()).apply{
//                cardElevation = 10f
//                radius = 4f
//
//                val imageView = ImageView(context).apply {
//                    setImageResource(resId)
//                    tag = index
//                    layoutParams = ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT
//                    )
//                }
//                addView(imageView)
//            }
//
//            val params = GridLayout.LayoutParams().apply {
//                width = GridLayout.LayoutParams.WRAP_CONTENT
//                height = GridLayout.LayoutParams.WRAP_CONTENT
//                setMargins(8, 8, 8, 8)
//                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
//                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
//
//            }
//            gridLayout.addView(cardView, params)


            val imageView = ImageView(context).apply {
                Log.d("MyFragment3", "Original cell size: $resId")
//                if (resId != null) {
//                    setImageResource(resId)
//                }
                setImageResource(resId)
                setOnClickListener{
                    showImageModal(resId)
                }

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // 초기 너비 0으로 설정
                    height = 0 // 초기 높이 0으로 설정
                    setMargins(8, 8, 8, 8)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    tag = index
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                elevation = 10f
                translationZ = 10f
            }
            gridLayout.addView(imageView)
            Log.d("MyFragment4", "ImageView added: $resId")
            gridLayout.requestLayout()
        }

        adjustSquareImage(gridLayout, imageResIds.size)
        Log.d("MyFragment5", "adjustSquareImage run")
    }

    private fun adjustSquareImage(gridLayout: GridLayout, childImageNumber: Int) {
        gridLayout.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                gridLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val totalWidth = gridLayout.width - gridLayout.paddingLeft - gridLayout.paddingRight
                val expectedMargin = 12
                val totalMargin = (gridLayout.columnCount - 1) * expectedMargin * 2 // 마진의 2배를 고려

                val cellSize = (totalWidth - totalMargin) / gridLayout.columnCount
                for (i in 0 until childImageNumber) {
                    val child = gridLayout.getChildAt(i) as ImageView
                    val originalSize = child.layoutParams.width
                    Log.d("MyFragment2", "Original cell size: $originalSize")
                    val params = GridLayout.LayoutParams() as GridLayout.LayoutParams
                    params.width = cellSize
                    params.height = cellSize
                    params.setMargins(8, 8, 8, 8)
                    Log.d("MyFragment2", "Calculated cell size: $cellSize")
                    child.layoutParams = params
                    child.requestLayout()
                    Log.d("MyFragment", "New size for child $i: ${params.width} x ${params.height}")

                }
            }
        })
    }

    private fun showImageModal(imageResId: Int){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image) // 별도의 레이아웃 파일
        val modalImageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        modalImageView.setImageResource(imageResId) // 큰 이미지로 변경
        dialog.show()
    }

}

