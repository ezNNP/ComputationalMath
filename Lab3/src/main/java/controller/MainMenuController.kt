package controller

import view.FileInputView
import view.MainMenuView
import view.PointsInputView

class MainMenuController(private val view: MainMenuView): Controller {

    init {
        addActionListeners()
    }

    override fun addActionListeners() {
        view.fileInputButton.addActionListener {
            run {
                view.frame.isEnabled = false
                val fileInputView = FileInputView(view.frame)
                FileInputController(fileInputView)
                fileInputView.showDialog()
            }
        }
        view.pointsInputButton.addActionListener {
            run {
                view.frame.isEnabled = false
                val pointsInputView = PointsInputView(view.frame, view.nInput.value as Int)
                PointsInputController(pointsInputView)
            }
        }
    }
}