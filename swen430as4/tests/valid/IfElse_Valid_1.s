
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 24(%rbp), %rax
	movq $10, %rbx
	cmpq %rbx, %rax
	jge label595
	movq $80, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $9, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $76, %rbx
	movq %rbx, 8(%rax)
	movq $69, %rbx
	movq %rbx, 16(%rax)
	movq $83, %rbx
	movq %rbx, 24(%rax)
	movq $83, %rbx
	movq %rbx, 32(%rax)
	movq $32, %rbx
	movq %rbx, 40(%rax)
	movq $84, %rbx
	movq %rbx, 48(%rax)
	movq $72, %rbx
	movq %rbx, 56(%rax)
	movq $65, %rbx
	movq %rbx, 64(%rax)
	movq $78, %rbx
	movq %rbx, 72(%rax)
	jmp label593
	jmp label594
label595:
	movq 24(%rbp), %rax
	movq $10, %rbx
	cmpq %rbx, %rax
	jle label597
	movq $104, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $12, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $71, %rbx
	movq %rbx, 8(%rax)
	movq $82, %rbx
	movq %rbx, 16(%rax)
	movq $69, %rbx
	movq %rbx, 24(%rax)
	movq $65, %rbx
	movq %rbx, 32(%rax)
	movq $84, %rbx
	movq %rbx, 40(%rax)
	movq $69, %rbx
	movq %rbx, 48(%rax)
	movq $82, %rbx
	movq %rbx, 56(%rax)
	movq $32, %rbx
	movq %rbx, 64(%rax)
	movq $84, %rbx
	movq %rbx, 72(%rax)
	movq $72, %rbx
	movq %rbx, 80(%rax)
	movq $65, %rbx
	movq %rbx, 88(%rax)
	movq $78, %rbx
	movq %rbx, 96(%rax)
	jmp label593
	jmp label596
label597:
	movq $56, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $6, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $69, %rbx
	movq %rbx, 8(%rax)
	movq $81, %rbx
	movq %rbx, 16(%rax)
	movq $85, %rbx
	movq %rbx, 24(%rax)
	movq $65, %rbx
	movq %rbx, 32(%rax)
	movq $76, %rbx
	movq %rbx, 40(%rax)
	movq $83, %rbx
	movq %rbx, 48(%rax)
	jmp label593
label596:
label594:
label593:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	movq $80, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $9, %rbx
	movq %rbx, 0(%rax)
	movq $76, %rbx
	movq %rbx, 8(%rax)
	movq $69, %rbx
	movq %rbx, 16(%rax)
	movq $83, %rbx
	movq %rbx, 24(%rax)
	movq $83, %rbx
	movq %rbx, 32(%rax)
	movq $32, %rbx
	movq %rbx, 40(%rax)
	movq $84, %rbx
	movq %rbx, 48(%rax)
	movq $72, %rbx
	movq %rbx, 56(%rax)
	movq $65, %rbx
	movq %rbx, 64(%rax)
	movq $78, %rbx
	movq %rbx, 72(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $1, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label599
	movq $1, %rax
	jmp label600
label599:
	movq $0, %rax
label600:
	movq %rax, %rdi
	call assertion
	movq $56, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $6, %rbx
	movq %rbx, 0(%rax)
	movq $69, %rbx
	movq %rbx, 8(%rax)
	movq $81, %rbx
	movq %rbx, 16(%rax)
	movq $85, %rbx
	movq %rbx, 24(%rax)
	movq $65, %rbx
	movq %rbx, 32(%rax)
	movq $76, %rbx
	movq %rbx, 40(%rax)
	movq $83, %rbx
	movq %rbx, 48(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $10, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label601
	movq $1, %rax
	jmp label602
label601:
	movq $0, %rax
label602:
	movq %rax, %rdi
	call assertion
	movq $104, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $12, %rbx
	movq %rbx, 0(%rax)
	movq $71, %rbx
	movq %rbx, 8(%rax)
	movq $82, %rbx
	movq %rbx, 16(%rax)
	movq $69, %rbx
	movq %rbx, 24(%rax)
	movq $65, %rbx
	movq %rbx, 32(%rax)
	movq $84, %rbx
	movq %rbx, 40(%rax)
	movq $69, %rbx
	movq %rbx, 48(%rax)
	movq $82, %rbx
	movq %rbx, 56(%rax)
	movq $32, %rbx
	movq %rbx, 64(%rax)
	movq $84, %rbx
	movq %rbx, 72(%rax)
	movq $72, %rbx
	movq %rbx, 80(%rax)
	movq $65, %rbx
	movq %rbx, 88(%rax)
	movq $78, %rbx
	movq %rbx, 96(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $11, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label603
	movq $1, %rax
	jmp label604
label603:
	movq $0, %rax
label604:
	movq %rax, %rdi
	call assertion
	movq $104, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $12, %rbx
	movq %rbx, 0(%rax)
	movq $71, %rbx
	movq %rbx, 8(%rax)
	movq $82, %rbx
	movq %rbx, 16(%rax)
	movq $69, %rbx
	movq %rbx, 24(%rax)
	movq $65, %rbx
	movq %rbx, 32(%rax)
	movq $84, %rbx
	movq %rbx, 40(%rax)
	movq $69, %rbx
	movq %rbx, 48(%rax)
	movq $82, %rbx
	movq %rbx, 56(%rax)
	movq $32, %rbx
	movq %rbx, 64(%rax)
	movq $84, %rbx
	movq %rbx, 72(%rax)
	movq $72, %rbx
	movq %rbx, 80(%rax)
	movq $65, %rbx
	movq %rbx, 88(%rax)
	movq $78, %rbx
	movq %rbx, 96(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $1212, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label605
	movq $1, %rax
	jmp label606
label605:
	movq $0, %rax
label606:
	movq %rax, %rdi
	call assertion
	movq $80, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $9, %rbx
	movq %rbx, 0(%rax)
	movq $76, %rbx
	movq %rbx, 8(%rax)
	movq $69, %rbx
	movq %rbx, 16(%rax)
	movq $83, %rbx
	movq %rbx, 24(%rax)
	movq $83, %rbx
	movq %rbx, 32(%rax)
	movq $32, %rbx
	movq %rbx, 40(%rax)
	movq $84, %rbx
	movq %rbx, 48(%rax)
	movq $72, %rbx
	movq %rbx, 56(%rax)
	movq $65, %rbx
	movq %rbx, 64(%rax)
	movq $78, %rbx
	movq %rbx, 72(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $-1212, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label607
	movq $1, %rax
	jmp label608
label607:
	movq $0, %rax
label608:
	movq %rax, %rdi
	call assertion
label598:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
