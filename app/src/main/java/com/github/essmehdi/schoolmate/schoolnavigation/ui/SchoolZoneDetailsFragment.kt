package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.github.essmehdi.schoolmate.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private const val ARG_NAME = "name"
private const val ARG_DESCRIPTION = "description"

/**
 * A simple [Fragment] subclass.
 * Use the [SchoolZoneDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SchoolZoneDetailsFragment : BottomSheetDialogFragment() {
  private var name: String? = null
  private var description: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      name = it.getString(ARG_NAME)
      description = it.getString(ARG_DESCRIPTION)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_school_zone_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<AppCompatTextView>(R.id.zone_name).text = name
    view.findViewById<AppCompatTextView>(R.id.zone_description).text = description
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name School zone title.
     * @param description School zone description.
     * @return A new instance of fragment SchoolZoneDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    @JvmStatic
    fun newInstance(name: String, description: String) =
      SchoolZoneDetailsFragment().apply {
        arguments = Bundle().apply {
          putString(ARG_NAME, name)
          putString(ARG_DESCRIPTION, description)
        }
      }
  }
}