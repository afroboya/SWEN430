
	.text
wl_f1:
	pushq %rbp
	movq %rsp, %rbp
	movq $72, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $8, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $71, %rbx
	movq %rbx, 8(%rax)
	movq $79, %rbx
	movq %rbx, 16(%rax)
	movq $84, %rbx
	movq %rbx, 24(%rax)
	movq $32, %rbx
	movq %rbx, 32(%rax)
	movq $66, %rbx
	movq %rbx, 40(%rax)
	movq $79, %rbx
	movq %rbx, 48(%rax)
	movq $79, %rbx
	movq %rbx, 56(%rax)
	movq $76, %rbx
	movq %rbx, 64(%rax)
	jmp label619
label619:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_f2:
	pushq %rbp
	movq %rsp, %rbp
	movq $64, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $7, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $71, %rbx
	movq %rbx, 8(%rax)
	movq $79, %rbx
	movq %rbx, 16(%rax)
	movq $84, %rbx
	movq %rbx, 24(%rax)
	movq $32, %rbx
	movq %rbx, 32(%rax)
	movq $73, %rbx
	movq %rbx, 40(%rax)
	movq $78, %rbx
	movq %rbx, 48(%rax)
	movq $84, %rbx
	movq %rbx, 56(%rax)
	jmp label620
label620:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	call wl_f2
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $64, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $7, %rcx
	movq %rcx, 0(%rbx)
	movq $71, %rcx
	movq %rcx, 8(%rbx)
	movq $79, %rcx
	movq %rcx, 16(%rbx)
	movq $84, %rcx
	movq %rcx, 24(%rbx)
	movq $32, %rcx
	movq %rcx, 32(%rbx)
	movq $73, %rcx
	movq %rcx, 40(%rbx)
	movq $78, %rcx
	movq %rcx, 48(%rbx)
	movq $84, %rcx
	movq %rcx, 56(%rbx)
	jmp label622
	movq $1, %rax
	jmp label623
label622:
	movq $0, %rax
label623:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	movq $0, %rax
	movq %rax, 8(%rsp)
	call wl_f1
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $72, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $8, %rcx
	movq %rcx, 0(%rbx)
	movq $71, %rcx
	movq %rcx, 8(%rbx)
	movq $79, %rcx
	movq %rcx, 16(%rbx)
	movq $84, %rcx
	movq %rcx, 24(%rbx)
	movq $32, %rcx
	movq %rcx, 32(%rbx)
	movq $66, %rcx
	movq %rcx, 40(%rbx)
	movq $79, %rcx
	movq %rcx, 48(%rbx)
	movq $79, %rcx
	movq %rcx, 56(%rbx)
	movq $76, %rcx
	movq %rcx, 64(%rbx)
	jmp label624
	movq $1, %rax
	jmp label625
label624:
	movq $0, %rax
label625:
	movq %rax, %rdi
	call assertion
label621:
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
